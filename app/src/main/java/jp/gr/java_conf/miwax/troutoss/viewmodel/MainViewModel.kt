package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.view.View
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.messenger.ShowSettingsActivityMessage

/**
 * Created by Tomoya Miwa on 2017/06/02.
 * Main画面用のViewModel
 */

class MainViewModel: BaseObservable() {

    val messenger = Messenger()

    fun onClickSettings(view: View) {
        messenger.send(ShowSettingsActivityMessage())
    }
}