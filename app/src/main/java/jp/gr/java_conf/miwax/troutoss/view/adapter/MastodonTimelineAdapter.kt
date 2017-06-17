package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Pageable
import com.sys1yagi.mastodon4j.api.Range
import com.sys1yagi.mastodon4j.api.entity.Status
import com.sys1yagi.mastodon4j.rx.RxFavourites
import com.sys1yagi.mastodon4j.rx.RxPublic
import com.sys1yagi.mastodon4j.rx.RxTimelines
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ContentStatusBinding
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonStatusHolder
import jp.gr.java_conf.miwax.troutoss.viewmodel.MastodonStatusViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await


/**
 * Created by Tomoya Miwa on 2017/05/02.
 * Mastodonのホーム用アダプタ
 */

class MastodonTimelineAdapter(private val client: MastodonClient,
                              type: Timeline,
                              private val account: MastodonAccount) :
        UltimateViewAdapter<MastodonTimelineAdapter.ViewHolder>() {

    val messenger = Messenger()

    private var pageable: Pageable<Status>? = null
    private val viewModels: MutableList<MastodonStatusViewModel> = mutableListOf()
    private val getTimeline: (Range) -> Single<Pageable<Status>> =
            when (type) {
                Timeline.HOME -> RxTimelines(client)::getHome
                Timeline.LOCAL -> RxPublic(client)::getLocalPublic
                Timeline.FEDERATED -> RxPublic(client)::getFederatedPublic
                Timeline.FAVOURITES -> RxFavourites(client)::getFavourites
                else -> RxTimelines(client)::getHome
            }

    enum class Timeline {
        HOME, LOCAL, FEDERATED, FAVOURITES
    }

    fun refresh(clear: Boolean = false): Deferred<Pair<Boolean, Int>> = async(CommonPool) {
        pageable = getTimeline(Range(limit = 20)).await()
        pageable?.let {
            val addable = if (clear) false else (viewModels.size > 0) && it.part.any { it.id == viewModels[0].statusId }
            if (addable) {
                val addStatuses = it.part.takeWhile { it.id != viewModels[0].statusId }
                viewModels.addAll(0, addStatuses.map { MastodonStatusViewModel(MastodonStatusHolder(it), client, account) })
                launch(UI) {
                    notifyItemRangeInserted(0, addStatuses.size)
                    updateStatusElapsed(addStatuses.size, viewModels.size - addStatuses.size)
                }.join()
            } else {
                viewModels.clear()
                viewModels.addAll(it.part.map { MastodonStatusViewModel(MastodonStatusHolder(it), client, account) })
                launch(UI) {
                    notifyDataSetChanged()
                }.join()
            }
            return@async Pair(addable, it.part.size)
        }
        return@async Pair(false, 0)
    }

    fun loadMoreOld(itemsCount: Int, lastPos: Int) = async(CommonPool) {
        if (pageable?.link == null) {
            // プログレス表示を消去
            launch(UI) { notifyItemChanged(lastPos) }.join()
            return@async 0
        }

        try {
            pageable = pageable?.let { getTimeline(it.nextRange(limit = 20)).await() }
        } catch (e: Exception) {
            // プログレス表示を消去
            launch(UI) { notifyItemChanged(lastPos) }.join()
            throw e
        }
        pageable?.let {
            val pos = viewModels.size
            viewModels.addAll(it.part.map { MastodonStatusViewModel(MastodonStatusHolder(it), client, account) })
            launch(UI) {
                notifyItemRangeInserted(pos, it.part.size)
                updateStatusElapsed(0, pos)
            }.join()
            return@async it.part.size
        }
        return@async 0
    }

    fun deleteStatus(id: Long) = async(UI) {
        viewModels.find { it.statusId == id }?.let {
            notifyItemRemoved(viewModels.indexOf(it))
            viewModels.remove(it)
        } ?: false
    }

    private fun updateStatusElapsed(positionStart: Int, itemCount: Int) {
        viewModels.slice(positionStart until itemCount).forEach { it.updateElapsed() }
    }

    override fun getAdapterItemCount(): Int {
        return viewModels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_status, parent, false)
        return ViewHolder(v, true)
    }

    class ViewHolder(itemView: View, normal: Boolean) : UltimateRecyclerviewViewHolder<View>(itemView) {
        var binding: ContentStatusBinding? = null
        val disposables = CompositeDisposable()

        init {
            if (normal) {
                binding = DataBindingUtil.bind(itemView)
                binding?.content?.movementMethod = LinkMovementMethod.getInstance()
                // ちらつき防止のため、通常不要なViewを非表示
                binding?.boostByText?.visibility = View.GONE
                binding?.boostByIcon?.visibility = View.GONE
                binding?.spoiler?.visibility = View.GONE
                binding?.spoilerSpace?.visibility = View.GONE
            }
        }
    }

    override fun generateHeaderId(position: Int): Long {
        return viewModels[position].statusId
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.disposables.clear()
        holder.binding?.let { binding ->
            binding.viewModel = viewModels[position]
            // ViewModelのメッセージを購読
            holder.disposables.add(
                    binding.viewModel.messenger.bus.doOnNext { messenger.send(it) }.subscribe()
            )
        }
    }


    override fun newFooterHolder(view: View): ViewHolder {
        return ViewHolder(view, false)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // do nothing
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return UltimateRecyclerviewViewHolder<View>(parent)
    }

    override fun newHeaderHolder(view: View): ViewHolder {
        return ViewHolder(view, false)
    }
}
