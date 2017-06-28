package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.RowHashtagBinding

/**
 * Created by Tomoya Miwa on 2017/06/26.
 * Mastodonのハッシュ用アダプター
 */

open class MastodonHashTagAdapter(private val hashtagsFlow: Flowable<List<String>>) :
        RecyclerView.Adapter<MastodonHashTagAdapter.ViewHolder>() {

    var hashtags: List<String> = arrayListOf()
    val disposable = CompositeDisposable()

    open fun onClickHashTag(hashtag: String) {}

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        disposable.add(
                hashtagsFlow
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            hashtags = it
                            notifyDataSetChanged()
                        }
        )
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposable.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_hashtag, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = hashtags[position]
        holder.binding.apply {
            hashtag.text = "#$tag"
            root.setOnClickListener { onClickHashTag(tag) }
        }
    }

    override fun getItemCount(): Int = hashtags.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: RowHashtagBinding = DataBindingUtil.bind(itemView)
    }
}
