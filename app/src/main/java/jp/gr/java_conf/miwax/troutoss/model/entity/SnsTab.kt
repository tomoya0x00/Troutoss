package jp.gr.java_conf.miwax.troutoss.model.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.util.*

/**
 * Created by Tomoya Miwa on 2017/04/30.
 * SNSタブ
 */

@PaperParcel
open class SnsTab(
        @PrimaryKey open var uuid: String = UUID.randomUUID().toString(),

        open var position: Int = 0,
        type: SnsTab.TabType = SnsTab.TabType.NONE,
        open var accountUuid: String = "",
        open var option: String = "",
        open var title: String = ""
) : RealmObject(), PaperParcelable, Cloneable {

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

    override fun hashCode(): Int {
        var result = 30

        result = 31 * result + position
        result = 31 * result + typeStr.hashCode()
        result = 31 * result + accountUuid.hashCode()
        result = 31 * result + option.hashCode()
        result = 31 * result + title.hashCode()

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SnsTab

        if (uuid != other.uuid) return false
        if (position != other.position) return false
        if (accountUuid != other.accountUuid) return false
        if (option != other.option) return false
        if (title != other.title) return false
        if (typeStr != other.typeStr) return false

        return true
    }

    override public fun clone(): SnsTab {
        return SnsTab(
                uuid = uuid,
                position = position,
                accountUuid = accountUuid,
                option = option,
                title = title,
                type = type
        )
    }

    companion object {
        @JvmField val CREATOR = PaperParcelSnsTab.CREATOR
    }
}
