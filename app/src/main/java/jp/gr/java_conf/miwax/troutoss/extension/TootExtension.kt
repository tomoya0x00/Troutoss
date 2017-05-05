package jp.gr.java_conf.miwax.troutoss.extension

/**
 * Created by Tomoya Miwa on 2017/05/05.
 * Toot用のExtension
 */

/**
 * 空白文字の削除
 * original: http://stackoverflow.com/questions/9589381/remove-extra-line-breaks-after-html-fromhtml
 */
fun CharSequence.trimTrailingWhitespace(): CharSequence {
    var i = this.length

    // loop back to the first non-whitespace character
    while (--i >= 0 && Character.isWhitespace(this[i])) {
    }

    return this.subSequence(0, i + 1)
}