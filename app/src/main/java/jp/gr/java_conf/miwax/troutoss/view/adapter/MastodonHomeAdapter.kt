package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Pageable
import com.sys1yagi.mastodon4j.api.Range
import com.sys1yagi.mastodon4j.api.entity.Status
import com.sys1yagi.mastodon4j.rx.RxTimelines
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ContentStatusBinding
import jp.gr.java_conf.miwax.troutoss.viewmodel.MastodonStatusViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await


/**
 * Created by Tomoya Miwa on 2017/05/02.
 * Mastodonのホーム用アダプタ
 */

class MastodonHomeAdapter(client: MastodonClient, private val context: Context) :
        UltimateViewAdapter<MastodonHomeAdapter.ViewHolder>() {

    val timelines = RxTimelines(client)
    var pageable: Pageable<Status>? = null
    val statuses: MutableList<Status> = mutableListOf()

    // TODO: 過去に遡っていく方向の追加対応
    // TODO: 最新のが少なければ追加していく対応
    // TODO: 最新のが多ければ置き換える対応
    // TODO: 仕切り線を書く

    init {
        refresh()
    }

    fun refresh() = launch(CommonPool) {
        // TODO: ロード中のプログレス表示
        // TODO: ロード失敗時のプログレス表示停止
        pageable = timelines.getHome(Range(limit = 20)).await()
        pageable?.let {
            statuses.clear()
            statuses.addAll(it.part)
            launch(UI) { notifyDataSetChanged() }
        }
    }

    fun loadMoreOld() = launch(CommonPool) {
        // TODO: ロード失敗時のプログレス表示停止
        pageable = pageable?.let { timelines.getHome(it.nextRange(limit = 20)).await() }
        pageable?.let {
            val pos = statuses.size
            statuses.addAll(it.part)
            launch(UI) { notifyItemRangeInserted(pos, it.part.size) }
        }
    }

    override fun getAdapterItemCount(): Int {
        return statuses.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.content_status, parent, false)
        return ViewHolder(v, true)
    }

    class ViewHolder(itemView: View?, normal: Boolean) : UltimateRecyclerviewViewHolder<View>(itemView) {
        var binding: ContentStatusBinding? = null

        init {
            if (normal) binding = DataBindingUtil.bind(itemView)
        }
    }

    override fun generateHeaderId(position: Int): Long {
        // TODO: IDをちゃんと生成する
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.binding?.viewModel = MastodonStatusViewModel(statuses[position], context)
    }

    override fun newFooterHolder(view: View?): ViewHolder {
        return ViewHolder(view, false)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        // do nothing
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return UltimateRecyclerviewViewHolder<View>(parent)
    }

    override fun newHeaderHolder(view: View?): ViewHolder {
        return ViewHolder(view, false)
    }
}
