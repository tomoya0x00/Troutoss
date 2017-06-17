package jp.gr.java_conf.miwax.troutoss.view.fragment


import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.marshalchen.ultimaterecyclerview.ui.divideritemdecoration.HorizontalDividerItemDecoration
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.rx.RxAccounts
import com.sys1yagi.mastodon4j.rx.RxReports
import com.sys1yagi.mastodon4j.rx.RxStatuses
import io.reactivex.disposables.CompositeDisposable
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.FragmentMastodonHomeBinding
import jp.gr.java_conf.miwax.troutoss.extension.showToast
import jp.gr.java_conf.miwax.troutoss.messenger.*
import jp.gr.java_conf.miwax.troutoss.model.CustomTabsHelper
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.convertDp2Pixel
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.view.activity.ImagesViewActivity
import jp.gr.java_conf.miwax.troutoss.view.activity.PostStatusActivity
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonTimelineAdapter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [MastodonTimelineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MastodonTimelineFragment : Fragment() {

    private var timeline: MastodonTimelineAdapter.Timeline? = null
    private var accountUuid: String? = null
    private var option: String? = null
    private var clearOnRefresh = false

    lateinit private var binding: FragmentMastodonHomeBinding
    private var adapter: MastodonTimelineAdapter? = null
    private val disposables = CompositeDisposable()
    private val timelineLayout: LinearLayoutManager
        get() = binding.timeline.layoutManager as LinearLayoutManager

    private val helper = MastodonHelper()
    private var account: MastodonAccount? = null
    private var client: MastodonClient? = null

    private val tabsIntent: CustomTabsIntent by lazy {
        CustomTabsHelper.createTabsIntent(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeline = arguments?.getString(ARG_TIMELINE)?.let { MastodonTimelineAdapter.Timeline.valueOf(it) }
        accountUuid = arguments?.getString(ARG_ACCOUNT_UUID)
        option = arguments?.getString(ARG_OPTION)
        account = accountUuid?.let { helper.loadAccountOf(it) }
        client = account?.let { helper.createAuthedClientOf(it) }
        timeline?.let { clearOnRefresh = it == MastodonTimelineAdapter.Timeline.FAVOURITES }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mastodon_home, container, false)

        adapter = client?.let {
            MastodonTimelineAdapter(it, timeline ?: MastodonTimelineAdapter.Timeline.HOME, account!!)
        }

        adapter?.let { adapter ->
            disposables.addAll(
                    adapter.messenger.register(ShowToastMessage::class.java).doOnNext {
                        Timber.d("received ShowToastMessage")
                        showToast(it.resId, Toast.LENGTH_SHORT)
                    }.subscribe(),
                    adapter.messenger.register(ShowImagesMessage::class.java).doOnNext {
                        Timber.d("received ShowImagesMessage")
                        ImagesViewActivity.startActivity(context, it.urls, it.index)
                    }.subscribe(),
                    adapter.messenger.register(OpenUrlMessage::class.java).doOnNext {
                        Timber.d("received OpenUrlMessage")
                        tabsIntent.launchUrl(activity, Uri.parse(it.url))
                    }.subscribe(),
                    adapter.messenger.register(ShowReplyActivityMessage::class.java).doOnNext { m ->
                        Timber.d("received ShowReplyActivityMessage")
                        accountUuid?.let { PostStatusActivity.startActivity(activity, AccountType.MASTODON, it, m.status) }
                    }.subscribe(),
                    adapter.messenger.register(ShowMastodonStatusMenuMessage::class.java).doOnNext { m ->
                        Timber.d("received ShowMastodonStatusMenuMessage")
                        if (m.myStatus) {
                            showMyStatusMenu(m.accountId, m.statusId, m.view)
                        } else {
                            showOtherStatusMenu(m.accountId, m.statusId, m.view)
                        }
                    }.subscribe()
            )
        }

        binding.timeline.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration((HorizontalDividerItemDecoration.Builder(context).build()))
            setDefaultOnRefreshListener { onRefresh() }
            setOnLoadMoreListener { itemsCount, lastPos -> onLoadMoreOld(itemsCount, lastPos) }
            setLoadMoreView(R.layout.center_progressbar)
            disableLoadmore()
            this@MastodonTimelineFragment.adapter?.let { setAdapter(it) }
        }

        onRefresh()

        return binding.root
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun onRefresh() = launch(UI) {
        binding.timeline.setRefreshing(true)
        try {
            adapter?.let { adapter ->
                val (added, loadedSize) = adapter.refresh(clearOnRefresh).await()
                if (loadedSize > 0) {
                    if (added) {
                        timelineLayout.scrollToPositionWithOffset(
                                timelineLayout.findFirstVisibleItemPosition(),
                                convertDp2Pixel(60).toInt())
                    } else {
                        timelineLayout.scrollToPositionWithOffset(0, 0)
                    }
                    binding.timeline.reenableLoadmore()
                }
            }
        } catch (e: Exception) {
            Timber.e("refresh failed: %s", e)
            showToast(R.string.comm_error, Toast.LENGTH_SHORT)
        } finally {
            binding.timeline.setRefreshing(false)
        }
    }

    private fun onLoadMoreOld(itemsCount: Int, lastPos: Int) = launch(UI) {
        try {
            val loadedSize = adapter?.loadMoreOld(itemsCount, lastPos)?.await()
            binding.timeline.disableLoadmore()
            loadedSize?.let { if (it > 0) binding.timeline.reenableLoadmore() }
        } catch (e: Exception) {
            Timber.e("loadMoreOld failed: %s", e)
            showToast(R.string.comm_error, Toast.LENGTH_SHORT)
        }
    }

    private fun showOtherStatusMenu(accountId: Long?, statusId: Long, view: View) {
        // TODO: Notification側にも実装
        val popup = PopupMenu(this.activity, view)
        popup.apply {
            menuInflater.inflate(R.menu.mastodon_other_status, popup.menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.mute -> {
                        accountId?.let { muteAccount(it) }
                        true
                    }
                    R.id.block -> {
                        accountId?.let { blockAccount(it) }
                        true
                    }
                    R.id.report -> {
                        accountId?.let { reportAccount(it, statusId) }
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    private fun showMyStatusMenu(accountId: Long?, statusId: Long, view: View) {
        // TODO: Notification側にも実装
        val popup = PopupMenu(this.activity, view)
        popup.apply {
            menuInflater.inflate(R.menu.mastodon_my_status, popup.menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete -> {
                        deleteStatus(statusId)
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    private fun deleteStatus(id: Long) {
        MaterialDialog.Builder(activity)
                .content(R.string.delete_toot)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive { _, _ ->
                    launch(UI) {
                        try {
                            async(CommonPool) {
                                client?.let { RxStatuses(it).deleteStatus(id).await() }
                            }.await()
                            adapter?.deleteStatus(id)?.await()
                            showToast(R.string.deleted)
                        } catch (e: Exception) {
                            Timber.e("deleteStatus failed: $e")
                            showToast(R.string.delete_failed)
                        }
                    }
                }.show()
    }

    private fun muteAccount(id: Long) {
        launch(UI) {
            try {
                async(CommonPool) {
                    client?.let { RxAccounts(it).postMute(id).await() }
                }.await()
                showToast(R.string.muted)
            } catch (e: Exception) {
                Timber.e("postMute failed: $e")
                showToast(R.string.mute_failed)
            }
        }
    }

    private fun blockAccount(id: Long) {
        launch(UI) {
            try {
                async(CommonPool) {
                    client?.let { RxAccounts(it).postBlock(id).await() }
                }.await()
                showToast(R.string.blocked)
            } catch (e: Exception) {
                Timber.e("postBlock failed: $e")
                showToast(R.string.block_failed)
            }
        }
    }

    private fun reportAccount(accountId: Long, statusId: Long) {
        MaterialDialog.Builder(activity)
                .positiveText(R.string.report)
                .negativeText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.report_hint), "", { _, input ->
                    val comment = input?.let { toString() } ?: ""
                    launch(UI) {
                        try {
                            async(CommonPool) {
                                client?.let { RxReports(it).postReport(accountId, statusId, comment).await() }
                            }.await()
                            showToast(R.string.reported)
                        } catch (e: Exception) {
                            Timber.e("postReport failed: $e")
                            showToast(R.string.report_failed)
                        }
                    }
                }).show()
    }

    companion object {
        private val ARG_TIMELINE = "timeline"
        private val ARG_ACCOUNT_UUID = "account_uuid"
        private val ARG_OPTION = "option"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param accountUuid
         * *
         * @param option
         * *
         * @return A new instance of fragment MastodonHomeFragment.
         */
        fun newInstance(timeline: MastodonTimelineAdapter.Timeline, accountUuid: String, option: String): MastodonTimelineFragment {
            val fragment = MastodonTimelineFragment()
            val args = Bundle()
            args.putString(ARG_TIMELINE, timeline.toString())
            args.putString(ARG_ACCOUNT_UUID, accountUuid)
            args.putString(ARG_OPTION, option)
            fragment.arguments = args
            return fragment
        }
    }
}
