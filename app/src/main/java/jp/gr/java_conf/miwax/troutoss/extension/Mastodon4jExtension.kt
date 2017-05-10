package jp.gr.java_conf.miwax.troutoss.extension

import com.sys1yagi.mastodon4j.api.entity.Attachment
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
