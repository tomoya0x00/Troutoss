package jp.gr.java_conf.miwax.troutoss.view.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import jp.gr.java_conf.miwax.troutoss.R


/**
 * Created by Tomoya Miwa on 2017/06/07.
 * original: https://stackoverflow.com/questions/35761636/is-it-possible-to-use-vectordrawable-in-buttons-and-textviews-using-androiddraw
 */

class DrawableCompatTextView : AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttrs(context, attrs)
    }

    internal fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val attributeArray = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.DrawableCompatTextView)

            var drawableLeft: Drawable? = null
            var drawableRight: Drawable? = null
            var drawableBottom: Drawable? = null
            var drawableTop: Drawable? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableLeft = attributeArray.getDrawable(R.styleable.DrawableCompatTextView_drawableLeftCompat)
                drawableRight = attributeArray.getDrawable(R.styleable.DrawableCompatTextView_drawableRightCompat)
                drawableBottom = attributeArray.getDrawable(R.styleable.DrawableCompatTextView_drawableBottomCompat)
                drawableTop = attributeArray.getDrawable(R.styleable.DrawableCompatTextView_drawableTopCompat)
            } else {
                val drawableLeftId = attributeArray.getResourceId(R.styleable.DrawableCompatTextView_drawableLeftCompat, -1)
                val drawableRightId = attributeArray.getResourceId(R.styleable.DrawableCompatTextView_drawableRightCompat, -1)
                val drawableBottomId = attributeArray.getResourceId(R.styleable.DrawableCompatTextView_drawableBottomCompat, -1)
                val drawableTopId = attributeArray.getResourceId(R.styleable.DrawableCompatTextView_drawableTopCompat, -1)

                if (drawableLeftId != -1)
                    drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId)
                if (drawableRightId != -1)
                    drawableRight = AppCompatResources.getDrawable(context, drawableRightId)
                if (drawableBottomId != -1)
                    drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId)
                if (drawableTopId != -1)
                    drawableTop = AppCompatResources.getDrawable(context, drawableTopId)
            }
            setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom)
            attributeArray.recycle()
        }
    }
}