package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import com.sys1yagi.mastodon4j.api.entity.Status
import com.sys1yagi.mastodon4j.rx.RxStatuses
import jp.gr.java_conf.miwax.troutoss.BR
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.messenger.CloseThisActivityMessage
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.messenger.ShowMastodonVisibilityDialog
import jp.gr.java_conf.miwax.troutoss.messenger.ShowToastMessage
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.view.dialog.MastodonVisibilityDialog
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import timber.log.Timber

/**
 * Created by Tomoya Miwa on 2017/05/17.
 * ステータス投稿用のViewModel
 */

class PostStatusViewModel(accountType: AccountType, accountUuid: String,
                          private val replyToId: Long? = null, replyToUsers: Array<String>? = null,
                          private var visibility: Status.Visibility) :
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

    @get:Bindable
    val visibilityIcon: Int
        get() = when (visibility) {
            Status.Visibility.Public -> R.drawable.public_earth
            Status.Visibility.Unlisted -> R.drawable.lock_open
            Status.Visibility.Private -> R.drawable.lock_close
            Status.Visibility.Direct -> R.drawable.direct
            else -> R.drawable.public_earth
        }

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
                        spoilerText = if (spoiler) spoilerText else null,
                        visibility = visibility
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

    fun onClickVisibility(view: View) {
        messenger.send(ShowMastodonVisibilityDialog())
    }

    fun onSelectVisibility(visibility: MastodonVisibilityDialog.Result) {
        Timber.d(visibility.name)
        this.visibility = when (visibility) {
            MastodonVisibilityDialog.Result.PUBLIC -> Status.Visibility.Public
            MastodonVisibilityDialog.Result.UNLISTED -> Status.Visibility.Unlisted
            MastodonVisibilityDialog.Result.PRIVATE -> Status.Visibility.Private
            MastodonVisibilityDialog.Result.DIRECT -> Status.Visibility.Direct
            else -> Status.Visibility.Public
        }
        notifyPropertyChanged(BR.visibilityIcon)
    }
}