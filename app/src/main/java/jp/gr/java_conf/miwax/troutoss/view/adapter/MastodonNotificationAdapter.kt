package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import com.marshalchen.ultimaterecyclerview.multiViewTypes.DataBinder
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Pageable
import com.sys1yagi.mastodon4j.api.Range
import com.sys1yagi.mastodon4j.api.entity.Notification
import com.sys1yagi.mastodon4j.rx.RxNotifications
import io.reactivex.disposables.CompositeDisposable
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ContentNotificationBinding
import jp.gr.java_conf.miwax.troutoss.databinding.ContentStatusBinding
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonStatusHolder
import jp.gr.java_conf.miwax.troutoss.viewmodel.MastodonNotificationViewModel
import jp.gr.java_conf.miwax.troutoss.viewmodel.MastodonStatusViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await

/**
 * Created by Tomoya Miwa on 2017/05/28.
 * Mastodonの通知用アダプタ
 */

class MastodonNotificationAdapter(client: MastodonClient, private val account: MastodonAccount) :
        UltimateDifferentViewTypeAdapter<MastodonNotificationAdapter.ViewType>() {

    val messenger = Messenger()

    private var pageable: Pageable<Notification>? = null
    private val notifications: MutableList<Notification> = mutableListOf()
    private val rxNotifications = RxNotifications(client)
    private val mentionBinder: MentionBinder

    enum class ViewType {
        MENTION, REBLOG, FAVOURITE, FOLLOW, NONE
    }

    init {
        mentionBinder = MentionBinder(this, notifications, client, account)
        putBinder(ViewType.MENTION, mentionBinder)
        putBinder(ViewType.REBLOG, NotificationBinder(this, notifications))
        putBinder(ViewType.FAVOURITE, NotificationBinder(this, notifications))
        putBinder(ViewType.FOLLOW, NotificationBinder(this, notifications))
        putBinder(ViewType.NONE, DummyBinder(this))
    }

    fun refresh() = async(CommonPool) {
        pageable = rxNotifications.getNotifications(Range(limit = 20)).await()
        pageable?.let {
            notifications.clear()
            notifications.addAll(it.part)
            launch(UI) { notifyDataSetChanged() }.join()
            return@async it.part.size
        }
        return@async 0
    }

    fun loadMoreOld(itemsCount: Int, lastPos: Int) = async(CommonPool) {
        if (pageable?.link == null) {
            // プログレス表示を消去
            launch(UI) { notifyItemChanged(lastPos) }.join()
            return@async 0
        }

        try {
            pageable = pageable?.let { rxNotifications.getNotifications(it.nextRange(limit = 20)).await() }
        } catch (e: Exception) {
            // プログレス表示を消去
            launch(UI) { notifyItemChanged(lastPos) }.join()
            throw e
        }
        pageable?.let {
            val pos = notifications.size
            notifications.addAll(it.part)
            launch(UI) {
                notifyItemRangeInserted(pos, it.part.size)
                mentionBinder.updateStatusElapsed(0, pos)
            }.join()
            return@async it.part.size
        }
        return@async 0
    }

    override fun getItemCount(): Int = notifications.size

    override fun getAdapterItemCount(): Int = 0

    override fun getEnumFromOrdinal(ordinal: Int): ViewType =
            ViewType.values()[ordinal]

    override fun getEnumFromPosition(position: Int): ViewType {
        if (notifications.size <= position) {
            return ViewType.NONE
        }

        return ViewType.valueOf(notifications[position].type.toUpperCase())
    }

    override fun onCreateViewHolder(parent: ViewGroup): DummyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.empty_view, parent, false)
        return DummyViewHolder(v)
    }

    class DummyViewHolder(itemView: View) : UltimateRecyclerviewViewHolder<View>(itemView)

    override fun generateHeaderId(position: Int): Long {
        return notifications[position].id
    }

    override fun newFooterHolder(view: View): DummyViewHolder {
        return DummyViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // do nothing
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return UltimateRecyclerviewViewHolder<View>(parent)
    }

    override fun newHeaderHolder(view: View): DummyViewHolder {
        return DummyViewHolder(view)
    }

    class MentionBinder(private val adapter: MastodonNotificationAdapter,
                        private val notifications: List<Notification>,
                        private val client: MastodonClient,
                        private  val account: MastodonAccount) :
            DataBinder<MentionBinder.ViewHolder>(adapter) {

        private val viewModels: MutableMap<Long, MastodonStatusViewModel> = hashMapOf()

        override fun getItemCount(): Int {
            return 1
        }

        override fun newViewHolder(parent: ViewGroup): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.content_status, parent, false)
            return ViewHolder(v)
        }

        override fun bindViewHolder(vh: ViewHolder, position: Int) {
            vh.disposables.clear()
            notifications[position].status?.let {
                if (!viewModels.containsKey(it.id)) {
                    viewModels[it.id] = MastodonStatusViewModel(MastodonStatusHolder(it), client, account)
                }
                vh.binding.viewModel = viewModels[it.id]
                // ViewModelのメッセージを購読
                vh.disposables.add(
                        vh.binding.viewModel.messenger.bus.doOnNext {
                            adapter.messenger.send(it)
                        }.subscribe()
                )
            }
        }

        fun updateStatusElapsed(positionStart: Int, itemCount: Int) {
            notifications.slice(positionStart until itemCount)
                    .filter { viewModels.containsKey(it.id) }
                    .forEach { viewModels[it.id]?.updateElapsed() }
        }

        class ViewHolder(itemView: View) : UltimateRecyclerviewViewHolder<View>(itemView) {
            var binding: ContentStatusBinding = DataBindingUtil.bind(itemView)
            val disposables = CompositeDisposable()

            init {
                // ちらつき防止のため、通常不要なViewを非表示
                binding.boostByText.visibility = View.GONE
                binding.boostByIcon.visibility = View.GONE
                binding.spoiler.visibility = View.GONE
                binding.spoilerSpace.visibility = View.GONE
            }
        }
    }

    class NotificationBinder(private val adapter: MastodonNotificationAdapter,
                             private val notifications: List<Notification>) :
            DataBinder<NotificationBinder.ViewHolder>(adapter) {

        override fun getItemCount(): Int {
            return 1
        }

        override fun newViewHolder(parent: ViewGroup): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.content_notification, parent, false)
            return ViewHolder(v)
        }

        override fun bindViewHolder(vh: ViewHolder, position: Int) {
            vh.disposables.clear()
            vh.binding.viewModel = MastodonNotificationViewModel(notifications[position])
            // ViewModelのメッセージを購読
            vh.disposables.add(
                    vh.binding.viewModel.messenger.bus.doOnNext {
                        adapter.messenger.send(it)
                    }.subscribe()
            )
        }

        class ViewHolder(itemView: View) : UltimateRecyclerviewViewHolder<View>(itemView) {
            var binding: ContentNotificationBinding = DataBindingUtil.bind(itemView)
            val disposables = CompositeDisposable()
        }
    }

    class DummyBinder(adapter: MastodonNotificationAdapter) :
            DataBinder<DummyBinder.ViewHolder>(adapter) {

        override fun getItemCount(): Int {
            return 1
        }

        override fun newViewHolder(parent: ViewGroup): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.empty_view, parent, false)
            return ViewHolder(v)
        }

        override fun bindViewHolder(vh: ViewHolder, position: Int) {}

        class ViewHolder(itemView: View) : UltimateRecyclerviewViewHolder<View>(itemView)
    }
}
