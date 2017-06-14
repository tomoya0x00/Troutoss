package jp.gr.java_conf.miwax.troutoss.extension

import android.os.Build
import com.facebook.stetho.okhttp3.StethoInterceptor
import jp.gr.java_conf.miwax.troutoss.model.TLSSocketFactory
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * Created by Tomoya Miwa on 2017/05/06.
 * OkHttp用のエクステンション
 */

fun OkHttpClientBuilderWithTimeout(): OkHttpClient.Builder {
    val timeout: Long = 15000
    return OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .enableTls12OnPreLollipop()
}

// original: https://github.com/square/okhttp/issues/2372
fun OkHttpClient.Builder.enableTls12OnPreLollipop(): OkHttpClient.Builder {
    if (Build.VERSION.SDK_INT in 16..21) {
        try {
            this.sslSocketFactory(TLSSocketFactory())
            val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build()

            val specs: MutableList<ConnectionSpec> = arrayListOf()
            specs.add(cs)
            specs.add(ConnectionSpec.COMPATIBLE_TLS)
            specs.add(ConnectionSpec.CLEARTEXT)

            this.connectionSpecs(specs)
        } catch (e: Exception) {
            Timber.e("Error while setting TLS 1.2: $e")
        }
    }

    return this
}