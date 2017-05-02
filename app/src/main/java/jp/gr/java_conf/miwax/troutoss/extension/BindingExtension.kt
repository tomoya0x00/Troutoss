package jp.gr.java_conf.miwax.troutoss.extension

import android.databinding.BindingAdapter
import android.os.Build
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

/**
 * Created by Tomoya Miwa on 2017/05/03.
 * DataBinding用の拡張関数
 */

@BindingAdapter("app:imageUrl")
fun ImageView.imageUrl(url: String?) {
    url?.let { Glide.with(context).load(it).into(this) }
}

@BindingAdapter("app:html")
fun TextView.setHtml(html: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.text = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    } else {
        val sppaned = Html.fromHtml(html)
        this.text = sppaned
    }
}