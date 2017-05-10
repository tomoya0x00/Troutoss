package jp.gr.java_conf.miwax.troutoss.messenger

import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor

/**
 * original:
 * https://github.com/amay077/StopWatchSample/blob/android_data_binding/StopWatchAppAndroid/app/src/main/java/com/amay077/stopwatchapp/frameworks/messengers/Messenger.java
 */
class Messenger {

    val bus: FlowableProcessor<Message> = PublishProcessor.create<Message>().toSerialized()

    fun send(message: Message) = bus.onNext(message)

    fun <T : Message> register(messageClazz: Class<out T>): Flowable<T> =
            bus.ofType(messageClazz).map { it }
}