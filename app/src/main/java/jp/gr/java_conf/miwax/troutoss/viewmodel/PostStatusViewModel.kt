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

class PostStatusViewModel(context: Context, accountType: AccountType, accountUuid: String,
                          private val replyToId: Long? = null, replyToUsers: Array<String>? = null) :
        BaseObservable() {

    val messenger = Messenger()
    private val statuses: RxStatuses?

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
    var spoilerText: String = ""

    @Bindable
    var status: String = ""

    @get:Bindable
    var statusCursor: Int = 0

    init {
        Timber.d("PostStatusViewModel accountUuid:$accountUuid, replyToId:$replyToId, replyToUsers:$replyToUsers")
        val helper = MastodonHelper()
        val client = helper.createAuthedClientOf(accountUuid)
        statuses = client?.let { RxStatuses(it) }

        replyToUsers?.let {
            status = it.joinToString(separator = " ", prefix = "@", postfix = " ")
            statusCursor = status.length
        }
    }

    fun onClickPost(view: View) {
        launch(CommonPool) {
            try {
                statuses?.postStatus(
                        status = status,
                        inReplyToId = replyToId,
                        mediaIds = null,
                        sensitive = false,
                        spoilerText = if (spoiler) spoilerText else null
                )?.await()
            } catch (e: Exception) {
                Timber.e("postStatus failed: %s", e.toString())
                messenger.send(ShowToastMessage(R.string.comm_error))
                return@launch
            }
            messenger.send(ShowToastMessage(R.string.post_success))
            messenger.send(CloseThisActivityMessage())
        }
    }
}