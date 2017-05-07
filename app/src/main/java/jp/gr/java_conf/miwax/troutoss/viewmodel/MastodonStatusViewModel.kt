package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.entity.Status
import jp.gr.java_conf.miwax.troutoss.R
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

/**
 * Created by Tomoya Miwa on 2017/05/02.
 * Mastodonのステータス用ViewModel
 */

class MastodonStatusViewModel(private val status: Status, val context: Context) : BaseObservable() {

    private val resources = context.resources

    // TODO: Boostなどのアイコン画像表示
    @get:Bindable
    val isBoost: Boolean
        get() = status.reblog != null

    @get:Bindable
    val boostBy: String
        get() = String.format(context.getString(R.string.mastodon_boost_by),
                status.account?.let { getNonEmptyName(it) } ?: "")

    @get:Bindable
    val avatarUrl: String?
        get() = showableAccount?.avatar

    @get:Bindable
    val displayName: String
        get() = showableAccount?.let { getNonEmptyName(it) } ?: ""

    @get:Bindable
    val userName: String
        get() = "@" + (showableAccount?.acct ?: "")

    @get:Bindable
    val elapsed: String
        get() {
            val now = ZonedDateTime.now()
            val createdAt = ZonedDateTime.parse(showableStatus?.createdAt)
            val elapsed = Duration.between(createdAt, now)
            val elapsedSec = elapsed.toMillis() / 1000
            return when {
                elapsedSec < 1 -> context.getString(R.string.status_now)
                elapsedSec < 60 -> resources.getQuantityString(R.plurals.status_second, elapsedSec.toInt(), elapsedSec)
                elapsedSec < 3600 -> resources.getQuantityString(R.plurals.status_minute, elapsed.toMinutes().toInt(), elapsed.toMinutes())
                elapsedSec < 3600 * 24 -> resources.getQuantityString(R.plurals.status_hour, elapsed.toHours().toInt(), elapsed.toHours())
                else -> resources.getQuantityString(R.plurals.status_day, elapsed.toDays().toInt(), elapsed.toDays())
            }
        }

    // TODO: CWなどを処理する
    @get:Bindable
    val content: String
        get() = showableStatus?.content ?: ""

    private val showableAccount: Account?
        get() = showableStatus?.account

    private val showableStatus: Status?
        get() = if (!isBoost) status else status.reblog

    private fun getNonEmptyName(account: Account): String =
            if (!account.displayName.isEmpty()) account.displayName else account.userName
}
