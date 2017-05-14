package jp.gr.java_conf.miwax.troutoss.messenger

import android.support.annotation.StringRes

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


