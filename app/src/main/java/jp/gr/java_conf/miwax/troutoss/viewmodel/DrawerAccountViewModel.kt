package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import jp.gr.java_conf.miwax.troutoss.messenger.*
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonTimelineAdapter

/**
 * Created by Tomoya Miwa on 2017/06/04.
 * ドロワー内アカウント用ViewModel
 */

class DrawerAccountViewModel(private val account: MastodonAccount) : BaseObservable() {

    val messenger = Messenger()

    @get:Bindable
    val accountName = account.userNameWithInstance

    fun onClicAccountSettings(view: View) {
        messenger.send(CloseDrawerMessage())
        messenger.send(ShowMastodonAccountSettingsActivityMessage(account.uuid))
    }

    fun onClickNotifications(view: View) {
        messenger.send(CloseDrawerMessage())
        messenger.send(ShowMastodonNotificationsActivityMessage(account.uuid))
    }

    fun onClickFavourite(view: View) {
        messenger.send(CloseDrawerMessage())
        messenger.send(ShowMastodonTimelineActivityMessage(MastodonTimelineAdapter.Timeline.FAVOURITES, account.uuid))
    }
}
