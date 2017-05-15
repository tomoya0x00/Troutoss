package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import io.realm.Realm
import io.realm.Sort
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.fragment.DummyFragment
import jp.gr.java_conf.miwax.troutoss.view.fragment.MastodonTimelineFragment

/**
 * Created by Tomoya Miwa on 2017/05/01.
 * SNSタブのアダプター
 */

class SnsTabAdapter(fm: FragmentManager?, val realm: Realm, val context: Context) : FragmentPagerAdapter(fm) {

    private val helper = MastodonHelper(context)
    // TODO: 動的なタブの追加削除移動に対応（http://qiita.com/akitaika_/items/80aeffb4bd28270bd609）
    private val tabs = realm.where(SnsTab::class.java)
            .findAllSorted(SnsTab::position.name, Sort.ASCENDING)

    init {
        tabs.addChangeListener { _, _ -> notifyDataSetChanged() }
    }

    override fun getItem(position: Int): Fragment {
        val tab = tabs[position]
        return when (tab.type) {
            SnsTab.TabType.MASTODON_HOME -> {
                MastodonTimelineFragment.newInstance(MastodonTimelineAdapter.Timeline.HOME, tab.accountUuid, tab.option)
            }
            SnsTab.TabType.MASTODON_FAVOURITES -> {
                MastodonTimelineFragment.newInstance(MastodonTimelineAdapter.Timeline.FAVOURITES, tab.accountUuid, tab.option)
            }
            SnsTab.TabType.MASTODON_LOCAL -> {
                MastodonTimelineFragment.newInstance(MastodonTimelineAdapter.Timeline.LOCAL, tab.accountUuid, tab.option)
            }
            SnsTab.TabType.MASTODON_FEDERATED -> {
                MastodonTimelineFragment.newInstance(MastodonTimelineAdapter.Timeline.FEDERATED, tab.accountUuid, tab.option)
            }
            else -> {
                DummyFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        val tab = tabs[position]
        val account = helper.loadAccountOf(tab.accountUuid)
        val countAccount = account?.let { helper.countAccountOf(it.instanceName) }

        if (tab.title.isNotEmpty()) {
            return tab.title
        } else {
            return when (tab.type) {
                SnsTab.TabType.MASTODON_HOME -> {
                    if (countAccount?.toInt() == 1) {
                        context.getString(R.string.mastodon_home_title_short, account.instanceName)
                    } else {
                        context.getString(R.string.mastodon_home_title_long, account?.userNameWithInstance ?: "")
                    }
                }
                SnsTab.TabType.MASTODON_FAVOURITES -> {
                    if (countAccount?.toInt() == 1) {
                        context.getString(R.string.mastodon_favourites_title_short, account.instanceName)
                    } else {
                        context.getString(R.string.mastodon_favourites_title_long, account?.userNameWithInstance ?: "")
                    }

                }
                SnsTab.TabType.MASTODON_NOTIFICATIONS -> {
                    if (countAccount?.toInt() == 1) {
                        context.getString(R.string.mastodon_notifications_title_short, account.instanceName)
                    } else {
                        context.getString(R.string.mastodon_notifications_title_long, account?.userNameWithInstance ?: "")
                    }
                }
                SnsTab.TabType.MASTODON_LOCAL -> {
                    context.getString(R.string.mastodon_local_title, account?.instanceName ?: "")
                }
                SnsTab.TabType.MASTODON_FEDERATED -> {
                    context.getString(R.string.mastodon_federated_title, account?.instanceName ?: "")
                }
                else -> {
                    tabs[position].type.toString()
                }
            }
        }
    }

    override fun getCount(): Int {
        return tabs.count()
    }

    fun getAccount(position: Int): Pair<AccountType, String> {
        val tab = tabs[position]
        return Pair(tab.type.accountType, tab.accountUuid)
    }
}