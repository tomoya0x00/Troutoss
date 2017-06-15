package jp.gr.java_conf.miwax.troutoss.extension

import android.databinding.BindingAdapter
import android.os.Build
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import timber.log.Timber

/**
 * Created by Tomoya Miwa on 2017/05/03.
 * DataBinding用の拡張関数
 */

@BindingAdapter("app:imageUrl")
fun ImageView.imageUrl(url: String?) {
    url?.let { Glide.with(context).load(it).into(this) }
}

@BindingAdapter("app:html")
@SuppressWarnings("deprecation")
fun TextView.setHtml(html: String) {
    Timber.d("before:\n$html")

    val newHtml = buildString {
        var inTag = false
        for (c in html) {
            if (inTag) {
                if (c == '>') {
                    inTag = false
                }
                append(c)
            } else {
                when (c) {
                    '<' -> {
                        inTag = true
                        append(c)
                    }
                    ' ' -> append("&nbsp;")
                    else -> append(c)
                }
            }
        }
    }

    Timber.d("after:\n$newHtml")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.text = Html.fromHtml(newHtml, Html.FROM_HTML_MODE_LEGACY).trimTrailingWhitespace()
    } else {
        this.text = Html.fromHtml(newHtml).trimTrailingWhitespace()
    }
}

@BindingAdapter("srcCompat")
fun ImageView.srcCompat(resourceId: Int) {
    this.setImageResource(resourceId)
}

