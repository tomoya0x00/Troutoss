package jp.gr.java_conf.miwax.troutoss.model.entity

import com.sys1yagi.mastodon4j.api.entity.Status
import jp.gr.java_conf.miwax.troutoss.extension.showableStatus

/**
 * Created by Tomoya Miwa on 2017/05/14.
 * Mastodonのstatusホルダー
 */

class MastodonStatusHolder(var status: Status) {
    var isShowSensitive = false
    var isShowContent = status.showableStatus().spoilerText.isEmpty()
    var isFavourited = status.showableStatus().isFavourited
    var isReblogged = status.showableStatus().isReblogged
}
