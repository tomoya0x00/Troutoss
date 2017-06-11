package jp.gr.java_conf.miwax.troutoss.model

import android.net.Uri
import android.webkit.MimeTypeMap
import jp.gr.java_conf.miwax.troutoss.App.Companion.appContext
import jp.gr.java_conf.miwax.troutoss.R.id.attachments
import jp.gr.java_conf.miwax.troutoss.extension.AttachmentType


/**
 * Created by Tomoya Miwa on 2017/06/11.
 * Attachment保持用のクラス
 */

class AttachmentHolder(private val attachments: MutableList<Attachment> = mutableListOf()) :
        MutableList<AttachmentHolder.Attachment> by attachments {

    data class Attachment(val uri: Uri, val mimeType: String, val type: AttachmentType)

    private val resolver = appContext.contentResolver

    private fun mimeTypeOf(uri: Uri): String {
        return resolver.getType(uri) ?:
                MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.let {
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
                } ?: "unknown/unknown"
    }

    private fun typeOf(uri: Uri) : AttachmentType {
        return typeOf(mimeTypeOf(uri))
    }

    private fun typeOf(mimeType: String): AttachmentType {
        return mimeType.let {
            when {
                it.startsWith("image", true) -> AttachmentType.IMAGE
                it.startsWith("video", true) -> AttachmentType.VIDEO
                else -> AttachmentType.UNKNOWN
            }
        }
    }

    fun add(uri: Uri): Boolean {
        if (!addable(uri)) {
            return false
        }

        val mimeType = mimeTypeOf(uri)
        attachments.add(Attachment(uri, mimeType, typeOf(mimeType)))
        return true
    }

    fun addable(uri: Uri): Boolean {
        if (!addable()) {
            return false
        }

        // 画像の後に動画を追加しようとしてないか
        if (attachments.isNotEmpty() && typeOf(uri) == AttachmentType.VIDEO) {
            return false
        }

        return true
    }

    fun addable(): Boolean {
        // 同時添付最大個数の超過確認
        if (attachments.size >= MAX_ATTACHMENT_COUNT) {
            return false
        }

        // すでに動画を含んでいるか
        if (attachments.filter { it.type == AttachmentType.VIDEO }.isNotEmpty()) {
            return false
        }

        return true
    }

    companion object {
        private val MAX_ATTACHMENT_COUNT = 4
    }
}
