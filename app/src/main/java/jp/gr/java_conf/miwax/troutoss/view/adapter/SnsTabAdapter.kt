package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import io.realm.Realm
import io.realm.Sort
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.DummyFragment

/**
 * Created by Tomoya Miwa on 2017/05/01.
 * SNSタブのアダプター
 */

class SnsTabAdapter(fm: FragmentManager?, val realm: Realm) : FragmentPagerAdapter(fm) {

    private val tabs = realm.where(SnsTab::class.java)
            .findAllSorted(SnsTab::position.name, Sort.ASCENDING)

    init {
        tabs.addChangeListener { _, _ -> notifyDataSetChanged() }
    }

    override fun getItem(position: Int): Fragment {
        return DummyFragment()
    }

    override fun getPageTitle(position: Int): CharSequence {
        // TODO: ページタイトルを生成
        return tabs[position].type.toString()
    }

    override fun getCount(): Int {
        return tabs.count()
    }

}