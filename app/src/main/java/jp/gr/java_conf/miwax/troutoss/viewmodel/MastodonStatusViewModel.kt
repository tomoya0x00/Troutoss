package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import com.sys1yagi.mastodon4j.api.entity.Status

/**
 * Created by Tomoya Miwa on 2017/05/02.
 * Mastodonのステータス用ViewModel
 */

class MastodonStatusViewModel(private val status: Status) : BaseObservable() {

    // TODO: Boostedの適切な表示

    @get:Bindable
    val avatarUrl: String?
        get() = status.account?.avatar

    // TODO: 長い名前の時に表示が隠れるのをどうにかする
    @get:Bindable
    val displayName: String
        get() = status.account?.displayName ?: status.account?.userName ?: ""

    // TODO: リモートフォローの場合はインスタンス名も含める？
    // TODO: アカウント名の先頭に@をつける
    @get:Bindable
    val userName: String
        get() = status.account?.userName ?: ""

    // TODO: 現在時刻からどれだけ前なのか？という表示に変更する
    @get:Bindable
    val createdAt: String
        get() = status.createdAt

    // TODO: CWなどを処理する
    // TODO: レイアウト調整（不要な行末の改行削除など？）
    // TODO: リンクの処理（とりあえずは外部ブラウザかな？）
    @get:Bindable
    val content: String
        get() = status.content
}
