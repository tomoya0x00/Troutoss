package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount

/**
 * Created by Tomoya Miwa on 2017/06/04.
 * ドロワー内アカウント用ViewModel
 */

class DrawerAccountViewModel(private val account: MastodonAccount) : BaseObservable() {

    val messenger = Messenger()

    @get:Bindable
    val accountName = account.userNameWithInstance
}
