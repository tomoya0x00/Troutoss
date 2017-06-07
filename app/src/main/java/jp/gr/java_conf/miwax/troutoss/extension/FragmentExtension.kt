package jp.gr.java_conf.miwax.troutoss.extension

import android.preference.PreferenceFragment
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import jp.gr.java_conf.miwax.troutoss.model.ToastHolder

/**
 * Created by Tomoya Miwa on 2017/06/06.
 * Fragment用のExtension
 */

fun Fragment.showToast(@StringRes resId: Int, duration: Int) {
    ToastHolder.showToast(this.activity, resId, duration)
}

fun Fragment.showToast(text: CharSequence, duration: Int) {
    ToastHolder.showToast(this.activity, text, duration)
}

fun PreferenceFragment.showToast(@StringRes resId: Int, duration: Int) {
    ToastHolder.showToast(this.activity, resId, duration)
}

fun PreferenceFragment.showToast(text: CharSequence, duration: Int) {
    ToastHolder.showToast(this.activity, text, duration)
}