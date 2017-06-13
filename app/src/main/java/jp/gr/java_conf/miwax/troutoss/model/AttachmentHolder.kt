package jp.gr.java_conf.miwax.troutoss.model

import android.net.Uri
import jp.gr.java_conf.miwax.troutoss.extension.AttachmentType
import jp.gr.java_conf.miwax.troutoss.extension.getMimeType
import paperparcel.PaperParcel
import paperparcel.PaperParcelable


/**
 * Created by Tomoya Miwa on 2017/06/11.
 * Attachment保持用のクラス
 */

@PaperParcel
class AttachmentHolder(val attachments: MutableList<Attachment> = mutableListOf()) :
        PaperParcelable, MutableList<AttachmentHolder.Attachment> by attachments {

    @PaperParcel
    data class Attachment(val uri: Uri, val mimeType: String, val type: AttachmentType) : PaperParcelable {
        companion object {
            @JvmField val CREATOR = PaperParcelAttachmentHolder_Attachment.CREATOR
        }
    }

    private fun typeOf(uri: Uri): AttachmentType {
        return typeOf(uri.getMimeType())
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

        val mimeType = uri.getMimeType()
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
        @JvmField val CREATOR = PaperParcelAttachmentHolder.CREATOR
    }
}
