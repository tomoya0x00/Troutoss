package jp.gr.java_conf.miwax.troutoss.extension

import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.entity.Attachment
import com.sys1yagi.mastodon4j.api.entity.Notification
import com.sys1yagi.mastodon4j.api.entity.Status
import jp.gr.java_conf.miwax.troutoss.App
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime
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


fun Status.isBoostable(): Boolean {
    return when (this.visibility) {
        "public", "unlisted" -> true
        else -> false
    }
}

fun Status.showableStatus(): Status {
    return this.reblog ?: this
}


fun Status.formatElapsed(): String
        = formatElapsed(this.createdAt)

fun Notification.formatElapsed(): String
        = formatElapsed(this.createdAt)

private fun formatElapsed(time: String): String {
    val now = ZonedDateTime.now()
    val createdAt = ZonedDateTime.parse(time)
    val elapsed = Duration.between(createdAt, now)
    val elapsedSec = elapsed.toMillis() / 1000
    return when {
        elapsedSec < 1 -> App.appResources.getString(R.string.status_now)
        elapsedSec < 60 -> App.appResources.getQuantityString(R.plurals.status_second, elapsedSec.toInt(), elapsedSec)
        elapsedSec < 3600 -> App.appResources.getQuantityString(R.plurals.status_minute, elapsed.toMinutes().toInt(), elapsed.toMinutes())
        elapsedSec < 3600 * 24 -> App.appResources.getQuantityString(R.plurals.status_hour, elapsed.toHours().toInt(), elapsed.toHours())
        else -> App.appResources.getQuantityString(R.plurals.status_day, elapsed.toDays().toInt(), elapsed.toDays())
    }
}

fun Account.getNonEmptyName(): String =
        if (this.displayName.isNotEmpty()) this.displayName else this.userName

enum class AttachmentType {
    IMAGE,
    VIDEO,
    GIFV,
    UNKNOWN
}