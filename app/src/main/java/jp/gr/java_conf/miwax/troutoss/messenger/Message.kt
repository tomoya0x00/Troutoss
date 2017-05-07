package jp.gr.java_conf.miwax.troutoss.messenger

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
class ShowToastMessage(val text: CharSequence) : Message
