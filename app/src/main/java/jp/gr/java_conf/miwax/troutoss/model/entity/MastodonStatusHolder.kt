package jp.gr.java_conf.miwax.troutoss.model.entity

import com.sys1yagi.mastodon4j.api.entity.Status

/**
 * Created by Tomoya Miwa on 2017/05/14.
 * Mastodonのstatusホルダー
 */

class MastodonStatusHolder(var status: Status) {
    var isShowSensitive = false
    var isFavourited = status.isFavourited
    var isReblogged = status.isReblogged
}
