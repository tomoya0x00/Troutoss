package jp.gr.java_conf.miwax.troutoss.extension

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by Tomoya Miwa on 2017/05/06.
 * OkHttp用のエクステンション
 */

fun OkHttpClientBuilderWithTimeout(): OkHttpClient.Builder {
    val timeout: Long = 10000
    return OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
}
