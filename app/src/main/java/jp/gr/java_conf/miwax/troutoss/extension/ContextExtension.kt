package jp.gr.java_conf.miwax.troutoss.extension

import android.content.Context
import android.os.Handler
import android.support.annotation.StringRes
import android.widget.Toast
import jp.gr.java_conf.miwax.troutoss.model.ToastHolder

/**
 * Created by Tomoya Miwa on 2017/06/06.
 * Context用のExtension
 */

fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Handler(mainLooper).post {
        ToastHolder.showToast(this, resId, duration)
    }
}

fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Handler(mainLooper).post {
        ToastHolder.showToast(this, text, duration)
    }
}