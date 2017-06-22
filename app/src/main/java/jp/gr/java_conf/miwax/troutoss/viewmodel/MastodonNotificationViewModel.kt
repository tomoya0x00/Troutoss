package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import com.sys1yagi.mastodon4j.api.entity.Notification
import jp.gr.java_conf.miwax.troutoss.App
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.extension.formatElapsed
import jp.gr.java_conf.miwax.troutoss.extension.getNonEmptyName
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.messenger.OpenUrlMessage
import java.net.URI

/**
 * Created by Tomoya Miwa on 2017/05/28.
 * Mastodonの通知用ViewModel
 */

class MastodonNotificationViewModel(private val notification: Notification) : BaseObservable() {

    val messenger = Messenger()

    @get:Bindable
    val follow: Boolean
        get() = notification.type == "follow"

    @get:Bindable
    val typeIcon: Int
        get() = when (notification.type) {
            "reblog" -> R.drawable.boost_on
            "favourite" -> R.drawable.favourite_on
            "follow" -> R.drawable.follow
            else -> R.drawable.favourite_on_inset
        }

    @get:Bindable
    val typeDetail: String
        get() = when (notification.type) {
            "reblog" -> notification.account?.let {
                String.format(App.appResources.getString(R.string.mastodon_boost_by), it.getNonEmptyName())
            } ?: ""
            "favourite" -> notification.account?.let {
                String.format(App.appResources.getString(R.string.mastodon_favourite_by), it.getNonEmptyName())
            } ?: ""
            "follow" -> notification.account?.let {
                String.format(App.appResources.getString(R.string.mastodon_follow_by), it.getNonEmptyName())
            } ?: ""
            else -> ""
        }

    @get:Bindable
    val status: String
        get() = (notification.status?.spoilerText ?: "") + (notification.status?.content ?: "")

    @get:Bindable
    val avatarUrl: String?
        get() {
            // TODO: 重複処理の削除
            val uri = URI(notification.account?.avatar)

            if (uri.isAbsolute) {
                return notification.account?.avatar
            } else {
                // ユーザーURLから絶対パス生成
                val userUrl = URI(notification.account?.url)
                val avatarUri = URI(userUrl.scheme, userUrl.host, notification.account?.avatar, null)
                return avatarUri.toString()
            }
        }

    @get:Bindable
    val displayName: String
        get() = notification.account?.getNonEmptyName() ?: ""

    @get:Bindable
    val userName: String
        get() = "@" + (notification.account?.acct ?: "")

    @get:Bindable
    val userNote: String
        get() = notification.account?.note ?: ""

    @get:Bindable
    val elapsed: String
        get() = notification.formatElapsed()

    fun onClickUser(view: View) {
        notification.account?.let { messenger.send(OpenUrlMessage(it.url)) }
    }
}
