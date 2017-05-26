package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sys1yagi.mastodon4j.api.entity.Attachment
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.RowAttachmentBinding
import jp.gr.java_conf.miwax.troutoss.extension.actualType
import jp.gr.java_conf.miwax.troutoss.extension.actualUrl
import jp.gr.java_conf.miwax.troutoss.extension.imageUrl
import jp.gr.java_conf.miwax.troutoss.extension.previewableUrl
import timber.log.Timber

/**
 * Created by Tomoya Miwa on 2017/05/09.
 * MastodonのAttachment用アダプター
 */

open class MastodonAttachmentAdapter(private val attachments: List<Attachment>) :
        RecyclerView.Adapter<MastodonAttachmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_attachment, parent, false)
        return ViewHolder(v)
    }

    open protected fun onClickImage(urls: Array<String>, index: Int) {}
    open protected fun onClickVideo(url: String) {}
    open protected fun onClickUnknown(url: String) {}

    override fun getItemCount(): Int = attachments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attachment = attachments[position]
        Timber.d("%d: type:%s, previewUrl:%s, url:%s, remote_url:%s",
                position, attachment.type, attachment.previewUrl, attachment.url, attachment.remoteUrl)

        // TODO: 画像ロード中のプログレス表示検討
        holder.binding.previewImage?.imageUrl(attachment.previewableUrl())
        when (attachment.actualType()) {
            "image" -> {
                holder.binding.previewText.text = ""
                holder.binding.previewText.visibility = View.GONE

                val urls = attachments.filter { it.actualType() == "image" }.map { it.actualUrl() }
                val index = urls.indexOf(attachment.actualUrl())
                holder.binding.preview.setOnClickListener { onClickImage(urls.toTypedArray(), index) }
            }
            "video", "gifv" -> {
                holder.binding.previewText.text = holder.binding.root.context.getString(R.string.video_content)
                holder.binding.previewText.visibility = View.VISIBLE

                holder.binding.preview.setOnClickListener { onClickVideo(attachment.actualUrl()) }
            }
            else -> {
                holder.binding.previewText.text = holder.binding.root.context.getString(R.string.unknown_content)
                holder.binding.previewText.visibility = View.VISIBLE

                holder.binding.preview.setOnClickListener { onClickUnknown(attachment.actualUrl()) }
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: RowAttachmentBinding = DataBindingUtil.bind(itemView)
    }
}
