package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.entity.Status
import jp.gr.java_conf.miwax.troutoss.BR
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.messenger.OpenUrlMessage
import jp.gr.java_conf.miwax.troutoss.messenger.ShowImagesMessage
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonAttachmentAdapter
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.net.URI


/**
 * Created by Tomoya Miwa on 2017/05/02.
 * Mastodonのステータス用ViewModel
 */

class MastodonStatusViewModel(private val status: Status, private val context: Context) : BaseObservable() {

    val messenger = Messenger()

    private val resources = context.resources

    private var showedSensitiveMedia = false

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
        get() {
            val uri = URI(showableAccount?.avatar)

            if (uri.isAbsolute) {
                return showableAccount?.avatar
            } else {
                // ユーザーURLから絶対パス生成
                val userUrl = URI(showableAccount?.url)
                val avatarUri = URI(userUrl.scheme, userUrl.host, showableAccount?.avatar, null)
                return avatarUri.toString()
            }
        }

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
            val createdAt = ZonedDateTime.parse(showableStatus.createdAt)
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
        get() = showableStatus.content

    @get:Bindable
    val hasAttachments: Boolean
        get() = showableStatus.mediaAttachments.isNotEmpty()

    @get:Bindable
    val attachmentAdapter: MastodonAttachmentAdapter =
            object : MastodonAttachmentAdapter(showableStatus.mediaAttachments) {
                override fun onClickImage(urls: Array<String>, index: Int) {
                    Timber.d("image clicked! urls:%s, index:%d", urls, index)
                    messenger.send(ShowImagesMessage(urls, index))
                }

                override fun onClickVideo(url: String) {
                    Timber.d("video clicked! url:%s", url)
                    messenger.send(OpenUrlMessage(url))
                }

                override fun onClickUnknown(url: String) {
                    Timber.d("unknown clicked! url:%s", url)
                    messenger.send(OpenUrlMessage(url))
                }
            }

    @get:Bindable
    val hideMedia: Boolean
        get() = !showedSensitiveMedia && showableStatus.isSensitive

    fun onClickShowMedia(view: View) {
        showedSensitiveMedia = true
        notifyPropertyChanged(BR.hideMedia)
    }

    fun onClickUser(view: View) {
        showableAccount?.let { messenger.send(OpenUrlMessage(it.url))}
    }

    private val showableAccount: Account?
        get() = showableStatus.account

    private val showableStatus: Status
        get() = if (!isBoost) status else status.reblog!!

    private fun getNonEmptyName(account: Account): String =
            if (!account.displayName.isEmpty()) account.displayName else account.userName
}
