package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import android.widget.CompoundButton
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.entity.Status
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.rx.RxStatuses
import jp.gr.java_conf.miwax.troutoss.BR
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.messenger.*
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonStatusHolder
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonAttachmentAdapter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.net.URI


/**
 * Created by Tomoya Miwa on 2017/05/02.
 * Mastodonのステータス用ViewModel
 */

class MastodonStatusViewModel(private val context: Context, private val holder: MastodonStatusHolder, client: MastodonClient) :
        BaseObservable() {

    val messenger = Messenger()

    private val resources = context.resources
    private val rxStatuses = RxStatuses(client)
    private val status: Status
        get() = holder.status

    @get:Bindable
    val isBoost: Boolean
        get() = status.reblog != null

    @get:Bindable
    val boostBy: String
        get() = String.format(context.getString(R.string.mastodon_boost_by),
                status.account?.let { getNonEmptyName(it) } ?: "")

    @get:Bindable
    val isBoosted: Boolean
        get() = holder.isReblogged

    @get:Bindable
    val isFavourited: Boolean
        get() = holder.isFavourited

    @get:Bindable
    val avatarUrl: String?
        get() {
            val uri = URI(showableAccount?.avatar)

            if (uri.isAbsolute) {
                return showableAccount?.avatar
            } else {
                // ユーザーURLから絶対パス生成
                val userUrl = URI(showableAccount?.url)
                val avatarUri = URI(userUrl.scheme, userUrl.host, showableAccount?.avatar, null)
                return avatarUri.toString()
            }
        }

    @get:Bindable
    val displayName: String
        get() = showableAccount?.let { getNonEmptyName(it) } ?: ""

    @get:Bindable
    val userName: String
        get() = "@" + (showableAccount?.acct ?: "")

    @get:Bindable
    val elapsed: String
        get() {
            val now = ZonedDateTime.now()
            val createdAt = ZonedDateTime.parse(showableStatus.createdAt)
            val elapsed = Duration.between(createdAt, now)
            val elapsedSec = elapsed.toMillis() / 1000
            return when {
                elapsedSec < 1 -> context.getString(R.string.status_now)
                elapsedSec < 60 -> resources.getQuantityString(R.plurals.status_second, elapsedSec.toInt(), elapsedSec)
                elapsedSec < 3600 -> resources.getQuantityString(R.plurals.status_minute, elapsed.toMinutes().toInt(), elapsed.toMinutes())
                elapsedSec < 3600 * 24 -> resources.getQuantityString(R.plurals.status_hour, elapsed.toHours().toInt(), elapsed.toHours())
                else -> resources.getQuantityString(R.plurals.status_day, elapsed.toDays().toInt(), elapsed.toDays())
            }
        }

    // TODO: CWなどを処理する
    @get:Bindable
    val content: String
        get() = showableStatus.content

    @get:Bindable
    val hasAttachments: Boolean
        get() = showableStatus.mediaAttachments.isNotEmpty()

    @get:Bindable
    val attachmentAdapter: MastodonAttachmentAdapter =
            object : MastodonAttachmentAdapter(showableStatus.mediaAttachments) {
                override fun onClickImage(urls: Array<String>, index: Int) {
                    Timber.d("image clicked! urls:%s, index:%d", urls, index)
                    messenger.send(ShowImagesMessage(urls, index))
                }

                override fun onClickVideo(url: String) {
                    Timber.d("video clicked! url:%s", url)
                    messenger.send(OpenUrlMessage(url))
                }

                override fun onClickUnknown(url: String) {
                    Timber.d("unknown clicked! url:%s", url)
                    messenger.send(OpenUrlMessage(url))
                }
            }

    @get:Bindable
    val hideMedia: Boolean
        get() = !holder.isShowSensitive && showableStatus.isSensitive

    fun onClickShowMedia(view: View) {
        holder.isShowSensitive = true
        notifyPropertyChanged(BR.hideMedia)
    }

    fun onClickUser(view: View) {
        showableAccount?.let { messenger.send(OpenUrlMessage(it.url)) }
    }

    fun onClickReply(view: View) {
        messenger.send(ShowReplyActivityMessage(showableStatus))
    }

    val boostChangeListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        launch(UI) {
            val post = when {
                !holder.isReblogged && checked -> rxStatuses::postReblog
                holder.isReblogged && !checked -> rxStatuses::postUnreblog
                else -> null
            }

            post?.let {
                try {
                    async(CommonPool) { it(status.id).await() }.await()
                    holder.isReblogged = !holder.isReblogged
                } catch (e: Mastodon4jRequestException) {
                    // レスポンスが422の場合は他のクライアントで操作されたと判断して無視
                    if (e.response?.code() != 422) {
                        Timber.e("post(Un)Reblog failed: %s", e)
                        messenger.send(ShowToastMessage(R.string.comm_error))
                    } else {
                        holder.isReblogged = !holder.isReblogged
                    }
                } finally {
                    notifyPropertyChanged(BR.boosted)
                }
            }
        }
    }

    val favouriteChangeListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        launch(UI) {
            val post = when {
                !holder.isFavourited && checked -> rxStatuses::postFavourite
                holder.isFavourited && !checked -> rxStatuses::postUnfavourite
                else -> null
            }

            post?.let {
                try {
                    holder.status = async(CommonPool) { it(status.id).await() }.await()
                    holder.isFavourited = !holder.isFavourited
                } catch (e: Mastodon4jRequestException) {
                    // レスポンスが422の場合は他のクライアントで操作されたと判断して無視
                    if (e.response?.code() != 422) {
                        Timber.e("post(Un)Favourite failed: %s", e)
                        messenger.send(ShowToastMessage(R.string.comm_error))
                    } else {
                        holder.isFavourited = !holder.isFavourited
                    }
                } finally {
                    notifyPropertyChanged(BR.favourited)
                }
            }
        }
    }

    fun onClickMoreActions(view: View) {

    }

    private val showableAccount: Account?
        get() = showableStatus.account

    private val showableStatus: Status
        get() = if (!isBoost) status else status.reblog!!

    private fun getNonEmptyName(account: Account): String =
            if (!account.displayName.isEmpty()) account.displayName else account.userName
}
