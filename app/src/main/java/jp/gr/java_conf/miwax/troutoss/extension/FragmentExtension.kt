package jp.gr.java_conf.miwax.troutoss.extension

import android.preference.PreferenceFragment
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.widget.Toast
import jp.gr.java_conf.miwax.troutoss.model.ToastHolder

/**
 * Created by Tomoya Miwa on 2017/06/06.
 * Fragment用のExtension
 */

fun Fragment.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    this.context?.let { ToastHolder.showToast(it, resId, duration) }
}

fun Fragment.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    this.context?.let { ToastHolder.showToast(it, text, duration) }
}

fun PreferenceFragment.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    ToastHolder.showToast(this.activity, resId, duration)
}

fun PreferenceFragment.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    ToastHolder.showToast(this.activity, text, duration)
}