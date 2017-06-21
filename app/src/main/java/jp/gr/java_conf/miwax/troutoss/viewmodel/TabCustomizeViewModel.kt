package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.SnsTabRepository
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.adapter.TabDragAdapter
import jp.gr.java_conf.miwax.troutoss.view.dialog.TabMenuDialog

/**
 * Created by Tomoya Miwa on 2017/06/18.
 * タブカスタマイズ用のViewModel
 */


class TabCustomizeViewModel(val adapter: TabDragAdapter) : BaseObservable() {

    val messenger = Messenger()

    private val helper = MastodonHelper()
    private val repository = SnsTabRepository(helper)

    fun onClickOk() {
        repository.replace(adapter.getReIndexedTabs())
    }

    fun onSelectAction(action: TabMenuDialog.Action, position: Int, tab: SnsTab) {
        when (action) {
            TabMenuDialog.Action.DELETE -> adapter.removeAt(position)
            TabMenuDialog.Action.RENAME -> {
                action.arg?.let { tab.title = it }
                adapter.notifyItemChanged(position)
            }
            TabMenuDialog.Action.RESET_NAME -> {
                tab.title = ""
                adapter.notifyItemChanged(position)
            }
            else -> {
            }
        }
    }
}
