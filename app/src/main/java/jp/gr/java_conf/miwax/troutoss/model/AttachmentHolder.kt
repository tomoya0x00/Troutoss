package jp.gr.java_conf.miwax.troutoss.model

import android.net.Uri
import android.webkit.MimeTypeMap
import jp.gr.java_conf.miwax.troutoss.App.Companion.appContext
import jp.gr.java_conf.miwax.troutoss.extension.AttachmentType


/**
 * Created by Tomoya Miwa on 2017/06/11.
 * Attachment保持用のクラス
 */

class AttachmentHolder(private val attachments: MutableList<Attachment> = mutableListOf()) :
        MutableList<AttachmentHolder.Attachment> by attachments {

    data class Attachment(val uri: Uri, val type: AttachmentType)

    private val resolver = appContext.contentResolver

    private fun typeOf(uri: Uri): AttachmentType {
        val mimeType = resolver.getType(uri) ?:
                MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.let {
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
                }

        return mimeType?.let {
            when {
                it.startsWith("image", true) -> AttachmentType.IMAGE
                it.startsWith("video", true) -> AttachmentType.VIDEO
                else -> AttachmentType.UNKNOWN
            }
        } ?: AttachmentType.UNKNOWN
    }

    fun add(uri: Uri): Boolean {
        if (!addable(uri)) {
            return false
        }

        attachments.add(Attachment(uri, typeOf(uri)))
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
        if (attachments.map { val (_, type) = it; type }
                .filter { it == AttachmentType.VIDEO }.isNotEmpty()) {
            return false
        }

        return true
    }

    companion object {
        private val MAX_ATTACHMENT_COUNT = 4
    }
}
