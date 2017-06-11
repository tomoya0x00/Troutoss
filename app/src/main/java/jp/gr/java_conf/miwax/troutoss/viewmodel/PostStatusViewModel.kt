package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.graphics.Color
import android.net.Uri
import android.view.View
import com.sys1yagi.mastodon4j.api.entity.Attachment
import com.sys1yagi.mastodon4j.api.entity.Status
import com.sys1yagi.mastodon4j.rx.RxMedia
import com.sys1yagi.mastodon4j.rx.RxStatuses
import jp.gr.java_conf.miwax.troutoss.BR
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.extension.readBytes
import jp.gr.java_conf.miwax.troutoss.messenger.CloseThisActivityMessage
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.messenger.ShowMastodonVisibilityDialog
import jp.gr.java_conf.miwax.troutoss.messenger.ShowToastMessage
import jp.gr.java_conf.miwax.troutoss.model.AttachmentHolder
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.view.adapter.AttachmentThumbnailAdapter
import jp.gr.java_conf.miwax.troutoss.view.dialog.MastodonVisibilityDialog
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber

/**
 * Created by Tomoya Miwa on 2017/05/17.
 * ステータス投稿用のViewModel
 */

class PostStatusViewModel(private val accountType: AccountType, accountUuid: String,
                          private val replyToId: Long? = null, replyToUsers: Array<String>? = null,
                          private var visibility: Status.Visibility) :
        BaseObservable() {

    val messenger = Messenger()

    private val statuses: RxStatuses?
    private val media: RxMedia?
    private var nowPosting = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.postable)
        }
    private val attachmentHolder = AttachmentHolder()

    @get:Bindable
    val thumbnailAdapter = AttachmentThumbnailAdapter(attachmentHolder)

    @get:Bindable
    val hasAttachments: Boolean
        get() = attachmentHolder.isNotEmpty()

    @get:Bindable
    val canAddAttachment: Boolean
        get() = attachmentHolder.addable()

    @Bindable
    var spoiler: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.spoiler)
        }

    @Bindable
    var sensitive: Boolean = false

    @set:Bindable
    var spoilerText: String = ""

    @Bindable
    var status: String = ""

    @get:Bindable
    var statusCursor: Int = 0

    val statusRestCount: Int
        get() = accountType.maxStatusLen - status.length

    @get:Bindable
    val statusCount: String
        get() = statusRestCount.toString()

    @get:Bindable
    val statusCountColor: Int
        get() = if (statusRestCount >= 0) Color.WHITE else Color.RED

    @get:Bindable
    val visibilityIcon: Int
        get() = when (visibility) {
            Status.Visibility.Public -> R.drawable.public_earth
            Status.Visibility.Unlisted -> R.drawable.lock_open
            Status.Visibility.Private -> R.drawable.lock_close
            Status.Visibility.Direct -> R.drawable.direct
            else -> R.drawable.public_earth
        }

    @get:Bindable
    val postable: Boolean
        get() = status.isNotEmpty() && statusRestCount >= 0 && !nowPosting

    init {
        Timber.d("PostStatusViewModel accountUuid:$accountUuid, replyToId:$replyToId, replyToUsers:$replyToUsers")
        val helper = MastodonHelper()
        val client = helper.createAuthedClientOf(accountUuid)
        statuses = client?.let { RxStatuses(it) }
        media = client?.let { RxMedia(it) }

        replyToUsers?.let {
            status = it.joinToString(separator = " ", prefix = "@", postfix = " ")
            statusCursor = status.length
        }
    }

    fun onClickPost(view: View) {
        nowPosting = true
        launch(CommonPool) {
            try {
                val mediaIds = postMediasIfNeed().await()?.map { it.id }
                statuses?.postStatus(
                        status = status,
                        inReplyToId = replyToId,
                        mediaIds = mediaIds,
                        sensitive = hasAttachments && sensitive,
                        spoilerText = if (spoiler) spoilerText else null,
                        visibility = visibility
                )?.await()
            } catch (e: Exception) {
                Timber.e("postStatus failed: %s", e.toString())
                messenger.send(ShowToastMessage(R.string.comm_error))
                nowPosting = false
                return@launch
            }
            messenger.send(ShowToastMessage(R.string.post_success))
            messenger.send(CloseThisActivityMessage())
        }
    }

    private fun postMediasIfNeed(): Deferred<List<Attachment>?> = async(CommonPool) {
        if (attachmentHolder.isEmpty()) {
            return@async null
        }

        val attachments: MutableList<Attachment> = mutableListOf()
        for (attachment in attachmentHolder) {
            val bytes = attachment.uri.readBytes().await()
            val requestFile = RequestBody.create(MediaType.parse(attachment.mimeType), bytes)
            val part = MultipartBody.Part.createFormData("file", "test", requestFile)
            try {
                // TODO: アップロード開始したことを表示
                media?.postMedia(part)?.let { attachments.add(it.await()) }
                // TODO: 成功したことを表示
            } catch (e: Exception) {
                // TODO: 失敗したことを表示
                // TODO: Exceptionを吐いて失敗したことを伝える
                throw e
            }
        }

        return@async attachments
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

    fun onStatusChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        notifyPropertyChanged(BR.statusCount)
        notifyPropertyChanged(BR.statusCountColor)
        notifyPropertyChanged(BR.postable)
    }

    fun onPickMedia(uri: Uri) {
        if (attachmentHolder.add(uri)) {
            thumbnailAdapter.notifyItemInserted(attachmentHolder.size - 1)
            notifyPropertyChanged(BR.hasAttachments)
            notifyPropertyChanged(BR.canAddAttachment)
        }
    }
}