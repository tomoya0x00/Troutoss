package jp.gr.java_conf.miwax.troutoss.extension

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics



/**
 * Created by Tomoya Miwa on 2017/06/09.
 * Firebase用のExtension
 */

fun FirebaseAnalytics.logAuthMastodonEvent(instanceName: String) {
    val bundle = Bundle().apply {
        putString("instance_name", instanceName)
    }
    this.logEvent("auth_mstdn", bundle)
}

fun FirebaseAnalytics.logPostEvent() {
    this.logEvent("post", null)
}