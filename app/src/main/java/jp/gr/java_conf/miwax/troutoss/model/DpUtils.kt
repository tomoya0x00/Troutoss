package jp.gr.java_conf.miwax.troutoss.model

import jp.gr.java_conf.miwax.troutoss.App.Companion.appResources


/**
 * Created by Tomoya Miwa on 2017/06/08.
 * Dp関連関数
 */

fun convertDp2Pixel(dp: Int): Float {
    return dp * appResources.displayMetrics.density
}