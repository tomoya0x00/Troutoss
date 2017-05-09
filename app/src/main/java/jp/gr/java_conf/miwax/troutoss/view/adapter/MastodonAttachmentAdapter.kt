package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sys1yagi.mastodon4j.api.entity.Attachment
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.RowAttachmentBinding
import jp.gr.java_conf.miwax.troutoss.extension.imageUrl
import timber.log.Timber
import java.net.URI

/**
 * Created by Tomoya Miwa on 2017/05/09.
 * MastodonのAttachment用アダプター
 */

class MastodonAttachmentAdapter(private val attachments: List<Attachment>) : RecyclerView.Adapter<MastodonAttachmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_attachment, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = attachments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attachment = attachments[position]
        Timber.d("%d: type:%s, previewUrl:%s, url:%s, remote_url:%s",
                position, attachment.type, attachment.previewUrl, attachment.url, attachment.remoteUrl)

        // TODO: urlが画像以外だった場合の対処（固定の動画画像にするとか）
        // TODO: 動画だった場合に再生マークを付ける
        // TODO: 画像ロード中のプログレス表示検討
        val url = when {
            URI(attachment.previewUrl).isAbsolute -> attachment.previewUrl
            URI(attachment.url).isAbsolute -> attachment.url
            else -> attachment.remoteUrl
        }

        holder.binding.previewImage?.imageUrl(url)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: RowAttachmentBinding = DataBindingUtil.bind(itemView)
    }
}
