package jp.gr.java_conf.miwax.troutoss.extension

import android.content.Context
import android.os.Handler
import android.support.annotation.StringRes
import android.widget.Toast

/**
 * Created by Tomoya Miwa on 2017/06/06.
 * Context用のExtension
 */


fun Context.showToast(@StringRes resId: Int, duration: Int) {
    Handler(mainLooper).post {
        Toast.makeText(this, resId, duration).show()
    }
}
