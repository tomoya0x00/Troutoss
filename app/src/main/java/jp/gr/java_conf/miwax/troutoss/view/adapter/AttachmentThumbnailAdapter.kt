package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ContentAttachmentThumbnailBinding
import jp.gr.java_conf.miwax.troutoss.model.AttachmentHolder
import jp.gr.java_conf.miwax.troutoss.viewmodel.AttachmentThumbnailViewModel


/**
 * Created by Tomoya Miwa on 2017/06/11.
 * 投稿時の添付サムネイル用アダプター
 */

open class AttachmentThumbnailAdapter(private val attachments: AttachmentHolder) :
        RecyclerView.Adapter<AttachmentThumbnailAdapter.ViewHolder>() {

    private val viewModels: MutableList<AttachmentThumbnailViewModel> =
            attachments.map { AttachmentThumbnailViewModel(it) }.toMutableList()

    open fun onEmpty() {}

    fun add(uri: Uri) {
        if (attachments.add(uri)) {
            viewModels.add(AttachmentThumbnailViewModel(attachments.last()))
            notifyItemInserted(attachments.size - 1)
        }
    }

    private fun removeAt(position: Int) {
        attachments.removeAt(position)
        viewModels.removeAt(position)
        notifyItemRemoved(position)
        if (attachments.isEmpty()) {
            onEmpty()
        }
    }

    fun enableProgressAt(position: Int, enable: Boolean) {
        viewModels[position].progress = enable
    }

    fun setResult(position: Int, success: Boolean) {
        viewModels[position].result = success
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_attachment_thumbnail, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.viewModel = viewModels[position]
        holder.binding.close.setOnClickListener { removeAt(position) }
    }

    override fun getItemCount(): Int = attachments.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ContentAttachmentThumbnailBinding = DataBindingUtil.bind(itemView)
    }
}