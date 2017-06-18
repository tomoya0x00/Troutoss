package jp.gr.java_conf.miwax.troutoss.model

import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import jp.gr.java_conf.miwax.troutoss.App
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab

/**
 * Created by Tomoya Miwa on 2017/04/30.
 * SNSタブのリポジトリ
 */

class SnsTabRepository(val helper: MastodonHelper) {

    fun addDefaultTabsOf(account: MastodonAccount) {
        Realm.getDefaultInstance().use { realm ->
            val basePos = realm.where(SnsTab::class.java)
                    .max(SnsTab::position.name)?.let { it.toInt() + 1 } ?: 0
            realm.executeTransaction {
                it.copyToRealm(SnsTab(position = basePos + 0, type = SnsTab.TabType.MASTODON_HOME, accountUuid = account.uuid))
                if (helper.countAccountOf(account.instanceName) > 1) return@executeTransaction
                // 初登録のインスタンスだったならローカルと連合タブも追加
                it.copyToRealm(SnsTab(position = basePos + 1, type = SnsTab.TabType.MASTODON_LOCAL, accountUuid = account.uuid))
                it.copyToRealm(SnsTab(position = basePos + 2, type = SnsTab.TabType.MASTODON_FEDERATED, accountUuid = account.uuid))
            }
        }
    }

    fun deleteTabsOf(accountUuid: String) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                it.where(SnsTab::class.java)
                        .equalTo(SnsTab::accountUuid.name, accountUuid)
                        .findAll()
                        .deleteAllFromRealm()
            }
        }
    }

    fun findFirstTabIndexOf(accountUuid: String): Int? {
        Realm.getDefaultInstance().use { realm ->
            val tabs = realm.where(SnsTab::class.java).findAll()
            val tab = realm.where(SnsTab::class.java)
                    .equalTo(SnsTab::accountUuid.name, accountUuid)
                    .findFirst()

            return tab?.let { tabs?.indexOf(it) }
        }
    }

    fun findAllSorted(): List<SnsTab> {
        Realm.getDefaultInstance().use { realm ->
            return findAllSorted(realm).let { realm.copyFromRealm(it) }
        }
    }

    fun findAllSorted(realm: Realm): RealmResults<SnsTab> {
        return realm.where(SnsTab::class.java)
                .findAllSorted(SnsTab::position.name, Sort.ASCENDING)
    }

    fun getTitleOf(tab: SnsTab): String {
        val account = helper.loadAccountOf(tab.accountUuid)
        val countAccount = account?.let { helper.countAccountOf(it.instanceName) }

        if (tab.title.isNotEmpty()) {
            return tab.title
        } else {
            return when (tab.type) {
                SnsTab.TabType.MASTODON_HOME -> {
                    if (countAccount?.toInt() == 1) {
                        App.appResources.getString(R.string.mastodon_home_title_short, account.instanceName)
                    } else {
                        App.appResources.getString(R.string.mastodon_home_title_long, account?.userNameWithInstance ?: "")
                    }
                }
                SnsTab.TabType.MASTODON_FAVOURITES -> {
                    if (countAccount?.toInt() == 1) {
                        App.appResources.getString(R.string.mastodon_favourites_title_short, account.instanceName)
                    } else {
                        App.appResources.getString(R.string.mastodon_favourites_title_long, account?.userNameWithInstance ?: "")
                    }

                }
                SnsTab.TabType.MASTODON_NOTIFICATIONS -> {
                    if (countAccount?.toInt() == 1) {
                        App.appResources.getString(R.string.mastodon_notifications_title_short, account.instanceName)
                    } else {
                        App.appResources.getString(R.string.mastodon_notifications_title_long, account?.userNameWithInstance ?: "")
                    }
                }
                SnsTab.TabType.MASTODON_LOCAL -> {
                    App.appResources.getString(R.string.mastodon_local_title, account?.instanceName ?: "")
                }
                SnsTab.TabType.MASTODON_FEDERATED -> {
                    App.appResources.getString(R.string.mastodon_federated_title, account?.instanceName ?: "")
                }
                else -> {
                    tab.type.toString()
                }
            }
        }
    }
}


