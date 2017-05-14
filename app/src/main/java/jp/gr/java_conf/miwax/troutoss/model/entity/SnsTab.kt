package jp.gr.java_conf.miwax.troutoss.model.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Tomoya Miwa on 2017/04/30.
 * SNSタブ
 */

open class SnsTab(
        @PrimaryKey open var uuid: String = UUID.randomUUID().toString(),

        open var position: Int = 0,
        type: SnsTab.TabType = SnsTab.TabType.NONE,
        open var accountUuid: String = "",
        open var option: String = "",
        open var title: String = ""
) : RealmObject() {

    protected open var typeStr: String = type.toString()

    enum class TabType {
        NONE,

        MASTODON_HOME,
        MASTODON_NOTIFICATIONS,
        MASTODON_FAVOURITES,
        MASTODON_LOCAL,
        MASTODON_FEDERATED
    }

    var type: TabType
        get() = TabType.valueOf(typeStr)
        set(value) {
            typeStr = value.toString()
        }
}
