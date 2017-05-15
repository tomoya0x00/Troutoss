package jp.gr.java_conf.miwax.troutoss.model.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Tomoya Miwa on 2017/04/25.
 * Mastodonアカウント情報
 */

open class MastodonAccount(
        @PrimaryKey open var uuid: String = UUID.randomUUID().toString(),

        open var instanceName: String = "",
        open var userName: String = "",
        open var accessToken: String = ""
) : RealmObject() {

    val userNameWithInstance: String
        get() = userName + "@" + instanceName
}