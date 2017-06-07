package jp.gr.java_conf.miwax.troutoss.model

import android.content.Context
import android.support.annotation.StringRes
import android.widget.Toast

/**
 * Created by Tomoya Miwa on 2017/06/06.
 * Toast用のMaster
 */

class ToastHolder {
    companion object {
        private var toast: Toast? = null

        fun showToast(context: Context, @StringRes resId: Int, duration: Int) {
            toast?.cancel()
            toast = Toast.makeText(context, resId, duration)
            toast?.show()
        }

        fun showToast(context: Context, text: CharSequence, duration: Int) {
            toast?.cancel()
            toast = Toast.makeText(context, text, duration)
            toast?.show()
        }
    }
}