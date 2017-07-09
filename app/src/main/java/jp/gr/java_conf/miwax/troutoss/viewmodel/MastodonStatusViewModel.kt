package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import android.widget.CompoundButton
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.entity.Status
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.rx.RxStatuses
import jp.gr.java_conf.miwax.troutoss.App.Companion.appResources
import jp.gr.java_conf.miwax.troutoss.BR
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.extension.*
import jp.gr.java_conf.miwax.troutoss.messenger.*
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonStatusHolder
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonAttachmentAdapter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import timber.log.Timber


/**
 * Created by Tomoya Miwa on 2017/05/02.
 * Mastodonのステータス用ViewModel
 */

class MastodonStatusViewModel(private val holder: MastodonStatusHolder,
                              client: MastodonClient,
                              private val myAccount: MastodonAccount) :
        BaseObservable() {

    val messenger = Messenger()

    private val rxStatuses = RxStatuses(client)
    private val status: Status
        get() = holder.status
    val statusId: Long
        get() = status.id

    @get:Bindable
    val isBoost: Boolean
        get() = status.reblog != null

    @get:Bindable
    val boostBy: String
        get() = String.format(appResources.getString(R.string.mastodon_boost_by_with_time),
                status.account?.let { getNonEmptyName(it) } ?: "",
                status.formatElapsed())

    @get:Bindable
    val isBoosted: Boolean
        get() = holder.isReblogged

    @get:Bindable
    val isFavourited: Boolean
        get() = holder.isFavourited

    @get:Bindable
    val avatarUrl: String?
        get() = showableAccount?.actualAvatarUrl()

    @get:Bindable
    val displayName: String
        get() = showableAccount?.getNonEmptyName() ?: ""

    @get:Bindable
    val userName: String
        get() = "@" + (showableAccount?.acct ?: "")

    @get:Bindable
    val elapsed: String
        get() = showableStatus.formatElapsed()

    @get:Bindable
    val content: String
        get() = showableStatus.content

    @get:Bindable
    val spoilerText: String
        get() = showableStatus.spoilerText

    @get:Bindable
    val hasContentWarning: Boolean
        get() = spoilerText.isNotEmpty()

    @get:Bindable
    val isShowContent
        get() = holder.isShowContent

    @get:Bindable
    val showSpoilerSpace: Boolean
        get() = hasContentWarning && isShowContent

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

    @get:Bindable
    val boostable: Boolean
        get() = showableStatus.isBoostable()

    @get:Bindable
    val boostsCount: String
        get() {
            val count = showableStatus.reblogsCount + if (isBoosted) 1 else 0
            return count.takeIf { it > 0 }?.toString() ?: ""
        }

    @get:Bindable
    val favouritesCount: String
        get() {
            val count = showableStatus.favouritesCount + if (isFavourited) 1 else 0
            return count.takeIf { it > 0 }?.toString() ?: ""
        }

    fun onClickShowContent(view: View) {
        holder.isShowContent = true
        notifyPropertyChanged(BR.showContent)
        notifyPropertyChanged(BR.showSpoilerSpace)
    }

    fun onClickShowMedia(view: View) {
        holder.isShowSensitive = true
        notifyPropertyChanged(BR.hideMedia)
    }

    fun onClickUser(view: View) {
        showableAccount?.let { messenger.send(OpenUrlMessage(it.url)) }
    }

    fun onClickBoostByUser(view: View) {
        status.account?.let { messenger.send(OpenUrlMessage(it.url)) }
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
                    val id = showableStatus.id
                    Timber.i("(un)reblog id:$id")
                    async(CommonPool) { it(id).await() }.await()
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
                    notifyChange()
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
                    val id = showableStatus.id
                    Timber.i("(un)favourite id:$id")
                    async(CommonPool) { it(id).await() }.await()
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
                    notifyChange()
                }
            }
        }
    }

    fun onClickMoreActions(view: View) {
        val myStatus = showableAccount?.let { it.acct == myAccount.userName } ?: false
        messenger.send(ShowMastodonStatusMenuMessage(showableAccount?.id, showableStatus.id, myStatus, view))
    }

    fun onClickStatus(view: View) {
        messenger.send(OpenUrlMessage(showableStatus.url))
    }

    fun updateElapsed() {
        notifyPropertyChanged(BR.elapsed)
    }

    private val showableAccount: Account?
        get() = showableStatus.account

    private val showableStatus: Status
        get() = status.showableStatus()

    private fun getNonEmptyName(account: Account): String =
            account.getNonEmptyName()
}
