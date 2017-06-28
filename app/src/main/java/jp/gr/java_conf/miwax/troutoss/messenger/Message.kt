package jp.gr.java_conf.miwax.troutoss.messenger

import android.support.annotation.StringRes
import android.view.View
import com.sys1yagi.mastodon4j.api.entity.Status
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonTimelineAdapter

/**
 * Created by Tomoya Miwa on 2017/05/07.
 * メッセンジャー用のメッセージ
 */

/**
 * original:
 * https://github.com/amay077/StopWatchSample/blob/android_data_binding/StopWatchAppAndroid/app/src/main/java/com/amay077/stopwatchapp/frameworks/messengers/Message.java
 */
interface Message

/**
 * トースト表示用のメッセージ
 */
class ShowToastMessage(@StringRes val resId: Int) : Message

/**
 * 複数画像表示用のメッセージ
 */

class ShowImagesMessage(val urls: Array<String>, val index: Int) : Message

/**
 * URLオープン用のメッセージ
 */
class OpenUrlMessage(val url: String) : Message

/**
 * Activity終了用のメッセージ
 */
class CloseThisActivityMessage : Message

/**
 * Reply画面表示用のメッセージ
 */

class ShowReplyActivityMessage(val status: Status) : Message

/**
 * MastodonVisibility選択ダイアログ表示用のメッセージ
 */

class ShowMastodonVisibilityDialog : Message

/**
 * 設定画面表示用のメッセージ
 */

class ShowSettingsActivityMessage : Message

/**
 * Mastodonアカウント設定画面表示用のメッセージ
 */

class ShowMastodonAccountSettingsActivityMessage(val accountUuid: String) : Message

/**
 * ドロワークローズ用のメッセージ
 */

class CloseDrawerMessage : Message

/**
 * Mastodon通知画面表示用のメッセージ
 */

class ShowMastodonNotificationsActivityMessage(val accountUuid: String) : Message

/**
 * Mastodonタイムライン画面表示用のメッセージ
 */

class ShowMastodonTimelineActivityMessage(
        val timeline: MastodonTimelineAdapter.Timeline,
        val accountUuid: String,
        val option: String? = null) : Message

/**
 * アカウント認証用のメッセージ
 */

class ShowAccountAuthActivityMessage : Message

/**
 * MastodonStatusメニュー表示用のメッセージ
 */

class ShowMastodonStatusMenuMessage(val accountId: Long?, val statusId: Long, val myStatus: Boolean, val view: View) : Message

/**
 * タブカスタマイズ画面表示用のメッセージ
 */

class ShowTabCustomizeActivityMessage : Message

