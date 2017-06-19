package jp.gr.java_conf.miwax.troutoss.extension

import android.os.Build
import com.facebook.stetho.okhttp3.StethoInterceptor
import jp.gr.java_conf.miwax.troutoss.model.TLSSocketFactory
import okhttp3.CipherSuite
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
            .retryOnConnectionFailure(true)
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .enableTls12OnPreLollipop()
}

// original: https://github.com/square/okhttp/issues/2372
fun OkHttpClient.Builder.enableTls12OnPreLollipop(): OkHttpClient.Builder {
    if (Build.VERSION.SDK_INT in 16..21) {
        try {
            val trustManager = TLSSocketFactory.systemDefaultTrustManager()
            trustManager?.let { this.sslSocketFactory(TLSSocketFactory(), it) }

            val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,

                            // 以下は okhttp 3 のデフォルト
                            // This is nearly equal to the cipher suites supported in Chrome 51, current as of 2016-05-25.
                            // All of these suites are available on Android 7.0; earlier releases support a subset of these
                            // suites. https://github.com/square/okhttp/issues/1972
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,

                            // Note that the following cipher suites are all on HTTP/2's bad cipher suites list. We'll
                            // continue to include them until better suites are commonly available. For example, none
                            // of the better cipher suites listed above shipped with Android 4.4 or Java 7.
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,

                            //pawoo
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256,

                            //https://www.ssllabs.com/ssltest/analyze.html?d=mastodon.cloud&latest
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256, // mastodon.cloud用 デフォルトにはない
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384, //mastodon.cloud用 デフォルトにはない

                            // https://www.ssllabs.com/ssltest/analyze.html?d=m.sighash.info
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384, // m.sighash.info 用 デフォルトにはない
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384, // m.sighash.info 用 デフォルトにはない
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256, // m.sighash.info 用 デフォルトにはない
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA // m.sighash.info 用 デフォルトにはない
                    )
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