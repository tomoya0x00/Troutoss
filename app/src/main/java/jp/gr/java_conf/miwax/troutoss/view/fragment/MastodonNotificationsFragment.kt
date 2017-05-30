package jp.gr.java_conf.miwax.troutoss.view.fragment


import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.StringRes
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.marshalchen.ultimaterecyclerview.ui.divideritemdecoration.HorizontalDividerItemDecoration
import io.reactivex.disposables.CompositeDisposable
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.FragmentMastodonNotificationBinding
import jp.gr.java_conf.miwax.troutoss.messenger.OpenUrlMessage
import jp.gr.java_conf.miwax.troutoss.messenger.ShowImagesMessage
import jp.gr.java_conf.miwax.troutoss.messenger.ShowReplyActivityMessage
import jp.gr.java_conf.miwax.troutoss.messenger.ShowToastMessage
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.view.activity.ImagesViewActivity
import jp.gr.java_conf.miwax.troutoss.view.activity.PostStatusActivity
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonNotificationAdapter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 * Use the [MastodonNotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MastodonNotificationsFragment : Fragment() {

    private var accountUuid: String? = null
    private var option: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var toast: Toast? = null

    lateinit private var binding: FragmentMastodonNotificationBinding
    private var adapter: MastodonNotificationAdapter? = null
    private val disposables = CompositeDisposable()

    private val tabsIntent: CustomTabsIntent by lazy {
        CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                .setStartAnimations(activity, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(activity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountUuid = arguments?.getString(ARG_ACCOUNT_UUID)
        option = arguments?.getString(ARG_OPTION)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mastodon_notification, container, false)

        val helper = MastodonHelper()
        val client = accountUuid?.let { helper.createAuthedClientOf(it) }
        adapter = client?.let { MastodonNotificationAdapter(it) }

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
                    }.subscribe()
            )
        }

        binding.notifications.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration((HorizontalDividerItemDecoration.Builder(context).build()))
            setDefaultOnRefreshListener { onRefresh() }
            setOnLoadMoreListener { _, _ -> onLoadMoreOld() }
            setLoadMoreView(R.layout.center_progressbar)
            setAdapter(this@MastodonNotificationsFragment.adapter)
        }

        onRefresh()

        return binding.root
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun onRefresh() = launch(UI) {
        binding.notifications.setRefreshing(true)
        try {
            adapter?.refresh()?.await()
        } catch (e: Exception) {
            Timber.e("refresh failed: %s", e)
            Toast.makeText(getContext(), R.string.comm_error, Toast.LENGTH_SHORT).show()
        } finally {
            binding.notifications.setRefreshing(false)
        }
    }

    private fun onLoadMoreOld() = launch(UI) {
        binding.notifications.disableLoadmore()
        try {
            adapter?.loadMoreOld()?.await()
        } catch (e: Exception) {
            Timber.e("loadMoreOld failed: %s", e)
            Toast.makeText(getContext(), R.string.comm_error, Toast.LENGTH_SHORT).show()
        } finally {
            binding.notifications.reenableLoadmore()
        }
    }

    private fun showToast(@StringRes resId: Int, duration: Int) {
        handler.post {
            toast?.cancel()
            toast = Toast.makeText(context, resId, duration)
            toast?.show()
        }
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
