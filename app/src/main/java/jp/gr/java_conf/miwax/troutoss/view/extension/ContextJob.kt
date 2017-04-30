package jp.gr.java_conf.miwax.troutoss.view.extension

import android.view.View
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.NonCancellable

/**
 * Created by Tomoya Miwa on 2017/04/30.
 * coroutineのライフサイクル用拡張
 */

interface JobHolder {
    val job: Job
}

val View.contextJob: Job
    get() = (context as? JobHolder)?.job ?: NonCancellable