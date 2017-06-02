package jp.gr.java_conf.miwax.troutoss.model

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import jp.gr.java_conf.miwax.troutoss.extension.OkHttpClientBuilderWithTimeout
import java.io.InputStream


/**
 * Created by Tomoya Miwa on 2017/06/02.
 * Stetho対応GlideModule
 */

class StethoGlideModule : OkHttpGlideModule() {
    override fun registerComponents(context: Context, glide: Glide) {
        val factory = OkHttpUrlLoader.Factory(OkHttpClientBuilderWithTimeout().build())
        glide.register(GlideUrl::class.java, InputStream::class.java, factory)
    }
}