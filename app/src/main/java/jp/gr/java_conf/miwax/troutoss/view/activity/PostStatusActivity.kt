package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import android.widget.Toast
import com.sys1yagi.mastodon4j.api.entity.Status
import io.reactivex.disposables.CompositeDisposable
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityPostStatusBinding
import jp.gr.java_conf.miwax.troutoss.extension.extractReplyToUsers
import jp.gr.java_conf.miwax.troutoss.extension.showToast
import jp.gr.java_conf.miwax.troutoss.messenger.CloseThisActivityMessage
import jp.gr.java_conf.miwax.troutoss.messenger.ShowMastodonVisibilityDialog
import jp.gr.java_conf.miwax.troutoss.messenger.ShowToastMessage
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.view.dialog.MastodonVisibilityDialog
import jp.gr.java_conf.miwax.troutoss.viewmodel.PostStatusViewModel
import timber.log.Timber

class PostStatusActivity : AppCompatActivity() {

    lateinit private var binding: ActivityPostStatusBinding
    lateinit private var viewModel: PostStatusViewModel

    private val disposables = CompositeDisposable()

    private val accountType: AccountType by lazy {
        intent.extras.getString(EXTRA_ACCOUNT_TYPE)?.let { AccountType.valueOf(it) } ?: AccountType.UNKNOWN
    }

    private val accountUuid: String by lazy { intent.extras.getString(EXTRA_ACCOUNT_UUID) }
    private val replyToId: Long? by lazy {
        if (intent.extras.containsKey(EXTRA_REPLY_TO_ID)) intent.extras.getLong(EXTRA_REPLY_TO_ID) else null
    }
    private val replyToUsers: Array<String>? by lazy { intent.extras.getStringArray(EXTRA_REPLY_TO_USERS) }
    private val visibility: Status.Visibility by lazy {
        intent.extras.getString(EXTRA_VISIBILITY)?.let { Status.Visibility.valueOf(it.capitalize()) } ?: Status.Visibility.Public
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_status)
        viewModel = PostStatusViewModel(accountType, accountUuid, replyToId, replyToUsers, visibility)
        binding.viewModel = viewModel

        disposables.addAll(
                viewModel.messenger.register(ShowToastMessage::class.java).doOnNext {
                    Timber.d("received ShowToastMessage")
                    showToast(it.resId, Toast.LENGTH_SHORT)
                }.subscribe(),
                viewModel.messenger.register(CloseThisActivityMessage::class.java).doOnNext {
                    Timber.d("received CloseThisActivityMessage")
                    finish()
                }.subscribe(),
                viewModel.messenger.register(ShowMastodonVisibilityDialog::class.java).doOnNext {
                    Timber.d("received ShowMastodonVisibilityDialog")
                }.flatMap {
                    MastodonVisibilityDialog(this@PostStatusActivity).show()
                }.doOnNext {
                    viewModel.onSelectVisibility(it)
                }.subscribe()
        )


        val account = MastodonHelper().loadAccountOf(accountUuid)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            account?.let { subtitle = it.userNameWithInstance }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    companion object {
        private val EXTRA_ACCOUNT_TYPE = "account_type"
        private val EXTRA_ACCOUNT_UUID = "account_uuid"
        private val EXTRA_REPLY_TO_ID = "reply_to_id"
        private val EXTRA_REPLY_TO_USERS = "reply_to_users"
        private val EXTRA_VISIBILITY = "visibility"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        fun startActivity(context: Context, accountType: AccountType, accountUuid: String, replyTo: Status? = null) {
            val intent = Intent(context, PostStatusActivity::class.java)
            intent.putExtra(PostStatusActivity.EXTRA_ACCOUNT_TYPE, accountType.toString())
            intent.putExtra(PostStatusActivity.EXTRA_ACCOUNT_UUID, accountUuid)

            replyTo?.let {
                intent.putExtra(PostStatusActivity.EXTRA_REPLY_TO_USERS, it.extractReplyToUsers(accountUuid))
                intent.putExtra(PostStatusActivity.EXTRA_REPLY_TO_ID, it.id)
                intent.putExtra(PostStatusActivity.EXTRA_VISIBILITY, it.visibility)
            }
            context.startActivity(intent)
        }
    }
}
