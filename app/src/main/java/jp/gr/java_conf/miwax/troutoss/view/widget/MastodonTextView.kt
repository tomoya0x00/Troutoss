package jp.gr.java_conf.miwax.troutoss.view.widget

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import jp.gr.java_conf.miwax.troutoss.model.CustomTabsHelper

/**
 * Created by Tomoya Miwa on 2017/06/08.
 * Mastodon用のTextView
 */

class MastodonTextView : AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        this.apply {
            transformationMethod = CustomTabsHelper.LinkTransformationMethod()
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
