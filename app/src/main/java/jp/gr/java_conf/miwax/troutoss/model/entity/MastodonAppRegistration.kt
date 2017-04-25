package jp.gr.java_conf.miwax.troutoss.model.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Tomoya Miwa on 2017/04/25.
 * Mastodonアプリ登録情報
 */

open class MastodonAppRegistration(
        @PrimaryKey open var uuid: String = UUID.randomUUID().toString(),

        open var instanceName: String = "",
        open var clientId: String = "",
        open var clientSecret: String = ""
) : RealmObject()
