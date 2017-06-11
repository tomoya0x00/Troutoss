package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.gr.java_conf.miwax.troutoss.App.Companion.appContext
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ContentAttachmentThumbnailBinding
import jp.gr.java_conf.miwax.troutoss.extension.AttachmentType
import jp.gr.java_conf.miwax.troutoss.extension.getBitmap
import jp.gr.java_conf.miwax.troutoss.model.AttachmentHolder
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch


/**
 * Created by Tomoya Miwa on 2017/06/11.
 * 投稿時の添付サムネイル用アダプター
 */

class AttachmentThumbnailAdapter(private val attachments: AttachmentHolder) :
        RecyclerView.Adapter<AttachmentThumbnailAdapter.ViewHolder>() {

    private val resolver = appContext.contentResolver

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_attachment_thumbnail, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attachment = attachments[position]

        launch(UI) {
            val bitmap = when (attachment.type) {
                AttachmentType.IMAGE -> getImageThumbnail(attachment.uri)
                AttachmentType.VIDEO -> getImageThumbnail(attachment.uri)
                else -> async(UI) { null }
            }

            bitmap.await()?.let { holder.binding.thumbnail.setImageBitmap(it) }
        }
    }

    fun getImageThumbnail(uri: Uri): Deferred<Bitmap?> = async(CommonPool) {
        // TODO: Thumbnail生成失敗時の対応

        val src = uri.getBitmap()
        src.await()?.let {
            val thumbnail = ThumbnailUtils.extractThumbnail(it, 512, 384,ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
            it.recycle()
            return@async thumbnail
        }

        return@async null
    }

    override fun getItemCount(): Int = attachments.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ContentAttachmentThumbnailBinding = DataBindingUtil.bind(itemView)
    }
}