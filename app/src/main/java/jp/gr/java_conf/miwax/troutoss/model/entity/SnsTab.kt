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
        NONE {
            override val accountType: AccountType
                get() = AccountType.UNKNOWN
        },

        MASTODON_HOME {
            override val accountType: AccountType
                get() = AccountType.MASTODON
        },
        MASTODON_NOTIFICATIONS {
            override val accountType: AccountType
                get() = AccountType.MASTODON
        },
        MASTODON_FAVOURITES {
            override val accountType: AccountType
                get() = AccountType.MASTODON
        },
        MASTODON_LOCAL {
            override val accountType: AccountType
                get() = AccountType.MASTODON
        },
        MASTODON_FEDERATED {
            override val accountType: AccountType
                get() = AccountType.MASTODON
        };

        abstract val accountType: AccountType
    }

    var type: TabType
        get() = TabType.valueOf(typeStr)
        set(value) {
            typeStr = value.toString()
        }
}
