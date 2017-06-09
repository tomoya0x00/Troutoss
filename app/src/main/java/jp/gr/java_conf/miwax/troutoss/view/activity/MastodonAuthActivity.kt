package jp.gr.java_conf.miwax.troutoss.view.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.rx.RxAccounts
import com.sys1yagi.mastodon4j.rx.RxApps
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityMastodonAuthBinding
import jp.gr.java_conf.miwax.troutoss.extension.OkHttpClientBuilderWithTimeout
import jp.gr.java_conf.miwax.troutoss.extension.logAuthMastodonEvent
import jp.gr.java_conf.miwax.troutoss.extension.showToast
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.SnsTabRepository
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import timber.log.Timber

class MastodonAuthActivity : android.support.v7.app.AppCompatActivity() {

    lateinit private var binding: ActivityMastodonAuthBinding
    private val helper: MastodonHelper by lazy { MastodonHelper() }
    private val analytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        binding = android.databinding.DataBindingUtil.setContentView<ActivityMastodonAuthBinding>(this, R.layout.activity_mastodon_auth)

        // アカウントを持っている場合は戻れるようにする
        if (helper.hasAccount()) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        // TODO: インスタンス名のValidation強化
        binding.loginButton.setOnClickListener { requestAuthCode() }
    }

    private fun requestAuthCode() = kotlinx.coroutines.experimental.launch(kotlinx.coroutines.experimental.android.UI) {
        binding.loginButton.isEnabled = false
        val instance = binding.instanceEdit.text.toString()
        val client = MastodonClient.Builder(instance, OkHttpClientBuilderWithTimeout(), Gson()).build()
        val apps = RxApps(client)
        try {
            val appRegistration = async(context + CommonPool) { helper.registerAppIfNeededTo(instance).await() }
            val url = async(context + CommonPool) { apps.apps.getOAuthUrl(appRegistration.await()!!.clientId, Scope(Scope.Name.ALL), helper.authCbUrl) }
            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url.await())))
        } catch (e: Exception) {
            Timber.e("Login failed: %s", e)
            showToast(R.string.login_failed, Toast.LENGTH_SHORT)
            binding.loginButton.isEnabled = true
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        timber.log.Timber.d("onNewIntent")

        intent.data?.let { onAuthCallBack(it) }
    }

    private fun onAuthCallBack(uri: Uri) = launch(UI) {
        // TODO: 通信中のプログレス表示をおこなう
        // TODO: 失敗時のエラー処理
        if (uri.toString().startsWith(helper.authCbUrl)) {
            val code = uri.getQueryParameter("code")
            if (code != null) {
                val instance = binding.instanceEdit.text.toString()
                val client = MastodonClient.Builder(instance, OkHttpClientBuilderWithTimeout(), Gson()).build()
                val apps = RxApps(client)
                val appRegistration = helper.loadAppRegistrationOf(instance)
                if (appRegistration != null) {
                    val mastodonAccount = async(context + CommonPool) {
                        val accessToken = apps.getAccessToken(
                                appRegistration.clientId,
                                appRegistration.clientSecret,
                                helper.authCbUrl,
                                code
                        ).await()
                        val authClient = MastodonClient.Builder(instance, OkHttpClientBuilderWithTimeout(), Gson())
                                .accessToken(accessToken.accessToken).build()
                        val accounts = RxAccounts(authClient)
                        val account = accounts.getVerifyCredentials().await()
                        return@async MastodonAccount(
                                instanceName = instance,
                                userName = account.userName,
                                accessToken = accessToken.accessToken
                        )
                    }.await()

                    // ログイン済みのアカウントか確認
                    if (helper.loadAccountOf(
                            instanceName = mastodonAccount.instanceName,
                            userName = mastodonAccount.userName) == null) {
                        helper.storeAccount(mastodonAccount)
                        SnsTabRepository(helper).addDefaultTabsOf(mastodonAccount)
                        showToast(R.string.login_and_add_tabs, Toast.LENGTH_SHORT)
                        analytics.logAuthMastodonEvent(mastodonAccount.instanceName)
                        val intent = Intent().apply {
                            putExtra(EXTRA_ACCOUNT_UUID, mastodonAccount.uuid)
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        showToast(R.string.logged_in_account_error, Toast.LENGTH_LONG)
                        binding.loginButton.isEnabled = true
                    }
                }
            } else {
                // User canceled!
                binding.loginButton.isEnabled = true
            }
        } else {
            // other
            binding.loginButton.isEnabled = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return true
        }
        return false
    }

    override fun onBackPressed() {
        // アカウントを持っていない場合はアプリを隠す
        if (helper.hasAccount()) {
            super.onBackPressed()
        } else {
            moveTaskToBack(true)
        }
    }

    companion object {

        val EXTRA_ACCOUNT_UUID = "account_uuid"
    }
}

