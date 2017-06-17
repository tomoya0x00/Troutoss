package jp.gr.java_conf.miwax.troutoss.view.fragment

import android.support.v4.app.Fragment
import android.text.InputType
import android.view.View
import android.widget.PopupMenu
import com.afollestad.materialdialogs.MaterialDialog
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.rx.RxAccounts
import com.sys1yagi.mastodon4j.rx.RxReports
import com.sys1yagi.mastodon4j.rx.RxStatuses
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.extension.showToast
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import timber.log.Timber

/**
 * Created by Tomoya Miwa on 2017/06/17.
 * Mastodon用のBaseFragment
 */

abstract class MastodonBaseFragment : Fragment() {

    abstract var client: MastodonClient?
    //abstract var adapter: MastodonAdapter<out RecyclerView.ViewHolder>?

    protected fun showOtherStatusMenu(accountId: Long?, statusId: Long, view: View) {
        val popup = PopupMenu(this.activity, view)
        popup.apply {
            menuInflater.inflate(R.menu.mastodon_other_status, popup.menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.mute -> {
                        accountId?.let { muteAccount(it) }
                        true
                    }
                    R.id.block -> {
                        accountId?.let { blockAccount(it) }
                        true
                    }
                    R.id.report -> {
                        accountId?.let { reportAccount(it, statusId) }
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    protected fun showOwnStatusMenu(accountId: Long?, statusId: Long, view: View) {
        val popup = PopupMenu(this.activity, view)
        popup.apply {
            menuInflater.inflate(R.menu.mastodon_my_status, popup.menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete -> {
                        deleteStatus(statusId)
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    abstract fun onDeleteStatus(id: Long): Deferred<Boolean>?

    protected fun deleteStatus(id: Long) {
        MaterialDialog.Builder(activity)
                .content(R.string.delete_toot)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive { _, _ ->
                    launch(UI) {
                        try {
                            async(CommonPool) {
                                client?.let { RxStatuses(it).deleteStatus(id).await() }
                            }.await()
                            onDeleteStatus(id)?.await()
                            showToast(R.string.deleted)
                        } catch (e: Exception) {
                            Timber.e("deleteStatus failed: $e")
                            showToast(R.string.delete_failed)
                        }
                    }
                }.show()
    }

    protected fun muteAccount(id: Long) {
        launch(UI) {
            try {
                async(CommonPool) {
                    client?.let { RxAccounts(it).postMute(id).await() }
                }.await()
                showToast(R.string.muted)
            } catch (e: Exception) {
                Timber.e("postMute failed: $e")
                showToast(R.string.mute_failed)
            }
        }
    }

    protected fun blockAccount(id: Long) {
        launch(UI) {
            try {
                async(CommonPool) {
                    client?.let { RxAccounts(it).postBlock(id).await() }
                }.await()
                showToast(R.string.blocked)
            } catch (e: Exception) {
                Timber.e("postBlock failed: $e")
                showToast(R.string.block_failed)
            }
        }
    }

    protected fun reportAccount(accountId: Long, statusId: Long) {
        MaterialDialog.Builder(activity)
                .positiveText(R.string.report)
                .negativeText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.report_hint), "", { _, input ->
                    val comment = input?.let { toString() } ?: ""
                    launch(UI) {
                        try {
                            async(CommonPool) {
                                client?.let { RxReports(it).postReport(accountId, statusId, comment).await() }
                            }.await()
                            showToast(R.string.reported)
                        } catch (e: Exception) {
                            Timber.e("postReport failed: $e")
                            showToast(R.string.report_failed)
                        }
                    }
                }).show()
    }
}
