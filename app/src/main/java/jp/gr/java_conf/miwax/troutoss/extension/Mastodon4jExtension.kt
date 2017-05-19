package jp.gr.java_conf.miwax.troutoss.extension

import com.sys1yagi.mastodon4j.api.entity.Attachment
import com.sys1yagi.mastodon4j.api.entity.Status
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import java.net.URI
import java.net.URLConnection

/**
 * Created by Tomoya Miwa on 2017/05/10.
 * mastodon4j用のエクステンション
 */

fun Attachment.actualUrl(): String {
    return when {
        URI(this.url).isAbsolute -> this.url
        else -> this.remoteUrl
    }
}

fun Attachment.previewableUrl(): String {
    val actualUrl = this.actualUrl()
    return when {
        URI(this.previewUrl).isAbsolute -> this.previewUrl
        URI(actualUrl).isAbsolute -> actualUrl
        else -> ""
    }
}

fun Attachment.actualType(): String {
    return when (this.type) {
        "image", "video", "gifv" -> this.type
        else -> {
            val mimeType = URLConnection.guessContentTypeFromName(this.actualUrl()) ?: ""
            when {
                mimeType.startsWith("image") -> "image"
                mimeType.startsWith("video") -> "video"
                else -> "unknown"
            }
        }
    }
}

fun Status.extractReplyToUsers(excludeAccountUuid: String): Array<String> {
    val myAccount = MastodonHelper().loadAccountOf(excludeAccountUuid)
    val replyToUsers = this.mentions.map { it.acct }.filter { it != myAccount?.userName }.toMutableList()
    this.account?.let { replyToUsers.add(0, it.acct) }
    return replyToUsers.toTypedArray()
}