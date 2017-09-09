package jp.gr.java_conf.miwax.troutoss.view.fragment


import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.marshalchen.ultimaterecyclerview.ui.divideritemdecoration.HorizontalDividerItemDecoration
import com.sys1yagi.mastodon4j.MastodonClient
import io.reactivex.disposables.CompositeDisposable
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.FragmentMastodonNotificationBinding
import jp.gr.java_conf.miwax.troutoss.extension.showToast
import jp.gr.java_conf.miwax.troutoss.messenger.*
import jp.gr.java_conf.miwax.troutoss.model.CustomTabsHelper
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.view.activity.ImagesViewActivity
import jp.gr.java_conf.miwax.troutoss.view.activity.PostStatusActivity
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonNotificationAdapter
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 * Use the [MastodonNotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MastodonNotificationsFragment : MastodonBaseFragment() {

    private var accountUuid: String? = null
    private var option: String? = null

    lateinit private var binding: FragmentMastodonNotificationBinding
    private var adapter: MastodonNotificationAdapter? = null

    private val disposables = CompositeDisposable()
    private val notificationsLayout: LinearLayoutManager
        get() = binding.notifications.layoutManager as LinearLayoutManager

    private val helper = MastodonHelper()
    private var account: MastodonAccount? = null
    override var client: MastodonClient? = null

    lateinit private var tabsIntent: CustomTabsIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountUuid = arguments?.getString(ARG_ACCOUNT_UUID)
        option = arguments?.getString(ARG_OPTION)
        account = accountUuid?.let { helper.loadAccountOf(it) }
        client = account?.let { helper.createAuthedClientOf(it) }
        tabsIntent = CustomTabsHelper.createTabsIntent(activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mastodon_notification, container, false)

        adapter = client?.let {
            MastodonNotificationAdapter(it, account!!)
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
                        if (!m.myStatus) {
                            showOtherStatusMenu(m.accountId, m.statusId, m.view)
                        }
                    }.subscribe()
            )
        }

        binding.notifications.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration((HorizontalDividerItemDecoration.Builder(context).build()))
            setDefaultOnRefreshListener { onRefresh() }
            setOnLoadMoreListener { itemsCount, lastPos -> onLoadMoreOld(itemsCount, lastPos) }
            setLoadMoreView(R.layout.center_progressbar)
            this@MastodonNotificationsFragment.adapter?.let { setAdapter(it) }
        }

        disableLoadMore()
        onRefresh()

        return binding.root
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun disableLoadMore() {
        binding.notifications.disableLoadmore()
        adapter?.customLoadMoreView?.visibility = View.GONE
    }

    private fun enableLoadMore() {
        binding.notifications.reenableLoadmore()
        adapter?.customLoadMoreView?.visibility = View.VISIBLE
    }

    private fun onRefresh() = launch(UI) {
        binding.notifications.setRefreshing(true)
        try {
            adapter?.let { adapter ->
                val loadedSize = adapter.refresh().await()
                if (loadedSize > 0) {
                    notificationsLayout.scrollToPositionWithOffset(0, 0)
                    enableLoadMore()
                }
            }
        } catch (e: Exception) {
            Timber.e("refresh failed: %s", e)
            showToast(R.string.comm_error, Toast.LENGTH_SHORT)
        } finally {
            binding.notifications.setRefreshing(false)
        }
    }

    private fun onLoadMoreOld(itemsCount: Int, lastPos: Int) = launch(UI) {
        try {
            val loadedSize = adapter?.loadMoreOld(itemsCount, lastPos)?.await()
            binding.notifications.disableLoadmore()
            loadedSize?.let {
                if (it > 0) {
                    enableLoadMore()
                } else {
                    disableLoadMore()
                }
            }
        } catch (e: Exception) {
            Timber.e("loadMoreOld failed: %s", e)
            showToast(R.string.comm_error, Toast.LENGTH_SHORT)
        }
    }

    override fun onDeleteStatus(id: Long): Deferred<Boolean>? {
        // 通知で自Statusの削除はおこなわないため、処理無し
        return null
    }

    override fun onReselected() {
        notificationsLayout.scrollToPositionWithOffset(0, 0)
    }

    companion object {
        private val ARG_ACCOUNT_UUID = "account_uuid"
        private val ARG_OPTION = "option"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param accountUuid
         * *
         * @param option
         * *
         * @return A new instance of fragment MastodonNotificationFragment.
         */
        fun newInstance(accountUuid: String, option: String): MastodonNotificationsFragment {
            val fragment = MastodonNotificationsFragment()
            val args = Bundle()
            args.putString(ARG_ACCOUNT_UUID, accountUuid)
            args.putString(ARG_OPTION, option)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
