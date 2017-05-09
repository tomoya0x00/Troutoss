package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.content.Context
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
import com.sys1yagi.mastodon4j.rx.RxPublic
import com.sys1yagi.mastodon4j.rx.RxTimelines
import io.reactivex.Single
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ContentStatusBinding
import jp.gr.java_conf.miwax.troutoss.viewmodel.MastodonStatusViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await


/**
 * Created by Tomoya Miwa on 2017/05/02.
 * Mastodonのホーム用アダプタ
 */

class MastodonTimelineAdapter(private val context: Context, client: MastodonClient, type: Timeline) :
        UltimateViewAdapter<MastodonTimelineAdapter.ViewHolder>() {

    private var pageable: Pageable<Status>? = null
    private val statuses: MutableList<Status> = mutableListOf()
    private val getTimeline: (Range) -> Single<Pageable<Status>> =
            when(type) {
                Timeline.HOME -> RxTimelines(client)::getHome
                Timeline.LOCAL -> RxPublic(client)::getLocalPublic
                Timeline.FEDERATED -> RxPublic(client)::getFederatedPublic
                else -> RxTimelines(client)::getHome
            }

    enum class Timeline {
        HOME, LOCAL, FEDERATED
    }

    fun refresh() = async(CommonPool) {
        pageable = getTimeline(Range(limit = 20)).await()
        pageable?.let {
            val addable = (statuses.size > 0) && it.part.any { it.id == statuses[0].id }
            if (addable) {
                val addStatuses = it.part.takeWhile { it.id != statuses[0].id }
                statuses.addAll(0, addStatuses)
                launch(UI) { notifyItemRangeInserted(0, addStatuses.size) }
            } else {
                statuses.clear()
                statuses.addAll(it.part)
                launch(UI) { notifyDataSetChanged() }
            }
        }
    }

    fun loadMoreOld() = async(CommonPool) {
        try {
            pageable = pageable?.let { getTimeline(it.nextRange(limit = 20)).await() }
        } catch (e: Exception) {
            // プログレス表示を消去
            launch(UI) { notifyDataSetChanged() }
            throw e
        }
        pageable?.let {
            val pos = statuses.size
            statuses.addAll(it.part)
            launch(UI) { notifyItemRangeInserted(pos, it.part.size) }
        }
    }

    override fun getAdapterItemCount(): Int {
        return statuses.size
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_status, parent, false)
        return ViewHolder(v, true)
    }

    class ViewHolder(itemView: View, normal: Boolean) : UltimateRecyclerviewViewHolder<View>(itemView) {
        var binding: ContentStatusBinding? = null

        init {
            if (normal) {
                binding = DataBindingUtil.bind(itemView)
                binding?.content?.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    override fun generateHeaderId(position: Int): Long {
        // TODO: IDをちゃんと生成する
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.viewModel = MastodonStatusViewModel(statuses[position], context)
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
