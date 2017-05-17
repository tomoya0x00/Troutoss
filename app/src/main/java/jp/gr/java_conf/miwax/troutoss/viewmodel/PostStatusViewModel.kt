package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import com.sys1yagi.mastodon4j.rx.RxStatuses
import jp.gr.java_conf.miwax.troutoss.BR
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.messenger.CloseThisActivityMessage
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.messenger.ShowToastMessage
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import timber.log.Timber

/**
 * Created by Tomoya Miwa on 2017/05/17.
 * ステータス投稿用のViewModel
 */

class PostStatusViewModel(private val context: Context, accountType: AccountType, accountUuid: String) : BaseObservable() {

    val messenger = Messenger()
    private val statuses: RxStatuses?

    init {
        val helper = MastodonHelper(context)
        val client = helper.createAuthedClientOf(accountUuid)
        statuses = client?.let { RxStatuses(it) }
    }

    @get:Bindable
    val hasAttachments: Boolean
        get() = false

    @Bindable
    var spoiler: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.spoiler)
        }

    @set:Bindable
    var status: String = ""

    fun onClickPost(view: View) {
        launch(CommonPool) {
            try {
                statuses?.postStatus(
                        status = this@PostStatusViewModel.status,
                        inReplyToId = null,
                        mediaIds = null,
                        sensitive = false,
                        spoilerText = null
                )?.await()
            } catch (e: Exception) {
                Timber.e("postStatus failed: %s", e)
                messenger.send(ShowToastMessage(R.string.comm_error))
                return@launch
            }
            messenger.send(ShowToastMessage(R.string.post_success))
            messenger.send(CloseThisActivityMessage())
        }
    }
}