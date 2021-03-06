package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.view.View
import jp.gr.java_conf.miwax.troutoss.messenger.*

/**
 * Created by Tomoya Miwa on 2017/06/02.
 * Main画面用のViewModel
 */

class MainViewModel : BaseObservable() {

    val messenger = Messenger()

    fun onClickSettings(view: View) {
        messenger.send(CloseDrawerMessage())
        messenger.send(ShowSettingsActivityMessage())
    }

    fun onClickAddAccount(view: View) {
        messenger.send(CloseDrawerMessage())
        messenger.send(ShowAccountAuthActivityMessage())
    }

    fun onClickCustomizeTab(view: View) {
        messenger.send(CloseDrawerMessage())
        messenger.send(ShowTabCustomizeActivityMessage())
    }
}