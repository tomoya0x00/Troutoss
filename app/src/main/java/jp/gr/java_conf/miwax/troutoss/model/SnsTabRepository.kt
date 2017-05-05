package jp.gr.java_conf.miwax.troutoss.model

import io.realm.Realm
import io.realm.Sort
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab

/**
 * Created by Tomoya Miwa on 2017/04/30.
 * SNSタブリポジトリ
 */

class SnsTabRepository(val helper: MastodonHelper) {

    fun addDefaultTabsFrom(account: MastodonAccount) {
        Realm.getDefaultInstance().use { realm ->
            val basePos = realm.where(SnsTab::class.java)
                    .max(SnsTab::position.name)?.let { it.toInt() + 1 } ?: 0
            realm.executeTransaction {
                it.copyToRealm(SnsTab(position = basePos + 0, type = SnsTab.TabType.MASTODON_HOME, accountUuid = account.uuid))
                it.copyToRealm(SnsTab(position = basePos + 1, type = SnsTab.TabType.MASTODON_NOTIFICATIONS, accountUuid = account.uuid))
                if (helper.hasAccountOf(account.instanceName)) return@executeTransaction
                // 初登録のインスタンスならローカルと連合タブも追加
                it.copyToRealm(SnsTab(position = basePos + 2, type = SnsTab.TabType.MASTODON_LOCAL, accountUuid = account.uuid))
                it.copyToRealm(SnsTab(position = basePos + 3, type = SnsTab.TabType.MASTODON_FEDERATED, accountUuid = account.uuid))
            }
        }
    }

}

