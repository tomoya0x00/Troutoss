package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import io.realm.Realm
import io.realm.Sort
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.fragment.DummyFragment
import jp.gr.java_conf.miwax.troutoss.view.fragment.MastodonHomeFragment

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
                MastodonHomeFragment.newInstance(tab.accountUuid, tab.option)
            }
            else -> {
                DummyFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        val tab = tabs[position]
        return when (tab.type) {
            SnsTab.TabType.MASTODON_HOME -> {
                String.format(context.getString(R.string.mastodon_home_title),
                        helper.loadAccountOf(tab.accountUuid)?.userNameWithinstance ?: "")
            }
            SnsTab.TabType.MASTODON_NOTIFICATIONS -> {
                String.format(context.getString(R.string.mastodon_notifications_title),
                        helper.loadAccountOf(tab.accountUuid)?.userNameWithinstance ?: "")
            }
            SnsTab.TabType.MASTODON_LOCAL -> {
                String.format(context.getString(R.string.mastodon_local_title),
                        helper.loadAccountOf(tab.accountUuid)?.instanceName ?: "")
            }
            SnsTab.TabType.MASTODON_FEDERATED -> {
                String.format(context.getString(R.string.mastodon_federated_title),
                        helper.loadAccountOf(tab.accountUuid)?.instanceName ?: "")
            }
            else -> {
                tabs[position].type.toString()
            }
        }
    }

    override fun getCount(): Int {
        return tabs.count()
    }

}