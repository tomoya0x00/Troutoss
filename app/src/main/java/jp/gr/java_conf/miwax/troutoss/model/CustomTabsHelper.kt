package jp.gr.java_conf.miwax.troutoss.model

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Parcel
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.Spanned
import android.text.method.TransformationMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import jp.gr.java_conf.miwax.troutoss.R


/**
 * Created by Tomoya Miwa on 2017/06/08.
 * CustomTabs用のヘルパークラス
 * original: https://medium.com/@nullthemall/make-textview-open-links-in-customtabs-12fdcf4bb684
 */

class CustomTabsHelper {

    companion object {
        fun createTabsIntent(context: Context): CustomTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .addDefaultShareMenuItem()
                .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .build()
    }

    class CustomTabsURLSpan : URLSpan {
        constructor(url: String) : super(url)
        constructor(src: Parcel) : super(src)

        override fun onClick(widget: View) {
            createTabsIntent(widget.context).launchUrl(widget.context, Uri.parse(url))
        }
    }

    class LinkTransformationMethod : TransformationMethod {

        override fun getTransformation(source: CharSequence, view: View): CharSequence {
            if (view is TextView) {
                val textView = view
                if (textView.text == null || textView.text !is Spannable) {
                    return source
                }
                val text = textView.text as Spannable
                val spans = text.getSpans(0, textView.length(), URLSpan::class.java)
                for (i in spans.indices.reversed()) {
                    val oldSpan = spans[i]
                    val start = text.getSpanStart(oldSpan)
                    val end = text.getSpanEnd(oldSpan)
                    val url = oldSpan.url
                    text.removeSpan(oldSpan)
                    text.setSpan(CustomTabsURLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                return text
            }
            return source
        }

        override fun onFocusChanged(view: View, sourceText: CharSequence, focused: Boolean, direction: Int, previouslyFocusedRect: Rect) {

        }
    }
}

