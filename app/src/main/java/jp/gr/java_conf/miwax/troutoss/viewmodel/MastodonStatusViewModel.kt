package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.entity.Status
import jp.gr.java_conf.miwax.troutoss.R

/**
 * Created by Tomoya Miwa on 2017/05/02.
 * Mastodonのステータス用ViewModel
 */

class MastodonStatusViewModel(private val status: Status, val context: Context) : BaseObservable() {

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

    // TODO: 長い名前の時に表示が隠れるのをどうにかする
    @get:Bindable
    val displayName: String
        get() = showableAccount?.let { getNonEmptyName(it) } ?: ""

    @get:Bindable
    val userName: String
        get() = "@" + (showableAccount?.acct ?: "")

    // TODO: 現在時刻からどれだけ前なのか？という表示に変更する
    @get:Bindable
    val createdAt: String
        get() = showableStatus?.createdAt ?: ""

    // TODO: CWなどを処理する
    // TODO: レイアウト調整（不要な行末の改行削除など？）
    // TODO: リンクの処理（とりあえずは外部ブラウザかな？）
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
