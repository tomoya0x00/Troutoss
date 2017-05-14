package jp.gr.java_conf.miwax.troutoss.view.behavior

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Created by Tomoya Miwa on 2016/07/13.
 * FABのアニメーション用Behavior
 */
class ScrollFABBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior() {
    private var translationY: Int = 0
    private var animatingRunning: Boolean = false

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout?,
                                     child: FloatingActionButton?, directTargetChild: View?, target: View?, nestedScrollAxes: Int): Boolean {

        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                nestedScrollAxes)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
                                target: View?, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)

        if (dyConsumed > 0 && !animatingRunning) {
            animateOut(child)
        } else if (dyConsumed < 0 && !animatingRunning) {
            animateIn(child)
        }
    }

    fun animateOut(view: View) {
        if (translationY == 0) {
            translationY = getTranslationY(view)
        }

        view.animate()
                .translationY(translationY.toFloat())
                .setDuration(DURATION_TIME.toLong())
                .setInterpolator(INTERPOLATOR)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        animatingRunning = false
                    }

                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        animatingRunning = true
                    }
                })
    }

    fun animateIn(view: View) {
        view.animate()
                .translationY(0f)
                .setDuration(DURATION_TIME.toLong())
                .setInterpolator(INTERPOLATOR)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        animatingRunning = false
                    }

                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        animatingRunning = true
                    }
                })
    }

    private fun getTranslationY(view: View): Int {
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        return view.height + lp.bottomMargin
    }

    companion object {

        private val INTERPOLATOR = AccelerateDecelerateInterpolator()
        private val DURATION_TIME = 200
    }
}