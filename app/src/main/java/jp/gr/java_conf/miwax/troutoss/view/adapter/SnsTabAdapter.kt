package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import io.realm.Realm
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.SnsTabRepository
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.fragment.DummyFragment
import jp.gr.java_conf.miwax.troutoss.view.fragment.MastodonNotificationsFragment
import jp.gr.java_conf.miwax.troutoss.view.fragment.MastodonTimelineFragment
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

/**
 * Created by Tomoya Miwa on 2017/05/01.
 * SNSタブのアダプター
 */

class SnsTabAdapter(fm: FragmentManager?, val realm: Realm) : FragmentPagerAdapter(fm) {

    private val helper = MastodonHelper()
    private val repository = SnsTabRepository(helper)
    private val tabs = repository.findAllSorted(realm)
    private var tabMap = mutableMapOf<Int, TabPosHolder>()

    init {
        tabs.addChangeListener { _, _ ->
            updateTabMap()
            launch(UI) { notifyDataSetChanged() }
        }
    }

    private data class TabPosHolder(val tabId: Int, var pos: Int, var gotPos: Int = pos)

    override fun getItem(position: Int): Fragment {
        if (tabs.isEmpty()) {
            return DummyFragment()
        }

        val tab = tabs[position]

        val fragment = when (tab.type) {
            SnsTab.TabType.MASTODON_HOME -> {
                MastodonTimelineFragment.newInstance(MastodonTimelineAdapter.Timeline.HOME, tab.accountUuid, tab.option)
            }
            SnsTab.TabType.MASTODON_FAVOURITES -> {
                MastodonTimelineFragment.newInstance(MastodonTimelineAdapter.Timeline.FAVOURITES, tab.accountUuid, tab.option)
            }
            SnsTab.TabType.MASTODON_NOTIFICATIONS -> {
                MastodonNotificationsFragment.newInstance(tab.accountUuid, tab.option)
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

        tabMap[fragment.hashCode()] = TabPosHolder(tab.hashCode(), position)

        Timber.d("getItem tab pos: $position, fragmentHash: ${fragment.hashCode()}")

        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence {
        if (tabs.isEmpty()) {
            return ""
        }

        return repository.getTitleOf(tabs[position])
    }

    override fun getCount(): Int {
        return tabs.count()
    }

    override fun getItemId(position: Int): Long {
        val id = tabs[position].hashCode().toLong()

        Timber.d("getItemId id: $id, pos: $position")

        return id
    }

    override fun getItemPosition(fragment: Any): Int {
        val pos = tabMap[fragment.hashCode()]?.let {
                if (it.gotPos == it.pos) {
                    POSITION_UNCHANGED
                } else {
                    it.gotPos = it.pos
                    it.pos
                }
        } ?: POSITION_NONE

        Timber.d("getItemPosition fragmentHash: ${fragment.hashCode()}, pos: $pos")

        return pos
    }

    fun getAccount(position: Int): Pair<AccountType, String> {
        if (tabs.isEmpty()) {
            return Pair(AccountType.UNKNOWN, "")
        }

        val tab = tabs[position]
        return Pair(tab.type.accountType, tab.accountUuid)
    }

    private fun updateTabMap() {
        val existTabIds = tabs.map { it.hashCode() }

        val newTabMap = mutableMapOf<Int, TabPosHolder>()
        for ((key, holder) in tabMap) {
            if (existTabIds.contains(holder.tabId)) {
                newTabMap[key] = TabPosHolder(holder.tabId, existTabIds.indexOf(holder.tabId), holder.gotPos)
            }
        }
        tabMap = newTabMap
    }
}