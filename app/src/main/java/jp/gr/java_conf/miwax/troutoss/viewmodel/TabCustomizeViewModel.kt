package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.SnsTabRepository
import jp.gr.java_conf.miwax.troutoss.view.adapter.TabDragAdapter

/**
 * Created by Tomoya Miwa on 2017/06/18.
 * タブカスタマイズ用のViewModel
 */


class TabCustomizeViewModel(val adapter: TabDragAdapter) : BaseObservable() {

    val messenger = Messenger()

    private val helper = MastodonHelper()
    private val repository = SnsTabRepository(helper)

    fun onClickOk() {
        repository.insertOrUpdate(adapter.getReIndexedTabs())
    }
}
