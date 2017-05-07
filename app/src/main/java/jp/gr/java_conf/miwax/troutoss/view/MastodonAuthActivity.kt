package jp.gr.java_conf.miwax.troutoss.view

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.rx.RxAccounts
import com.sys1yagi.mastodon4j.rx.RxApps
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityMastodonAuthBinding
import jp.gr.java_conf.miwax.troutoss.extension.OkHttpClientBuilderWithTimeout
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.SnsTabRepository
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import timber.log.Timber

class MastodonAuthActivity : AppCompatActivity() {

    companion object {
        val INTENT_ACCOUNT_UUID = "account_uuid"
    }

    lateinit private var binding: ActivityMastodonAuthBinding
    private val helper: MastodonHelper by lazy { MastodonHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMastodonAuthBinding>(this, R.layout.activity_mastodon_auth)

        // TODO: インスタンス名のValidation強化
        binding.loginButton.setOnClickListener { requestAuthCode() }
    }

    private fun requestAuthCode() = launch(UI) {
        val instance = binding.instanceEdit.text.toString()
        val client = MastodonClient.Builder(instance, OkHttpClientBuilderWithTimeout(), Gson()).build()
        val apps = RxApps(client)
        try {
            val appRegistration = async(context + CommonPool) { helper.registerAppIfNeededTo(instance).await() }
            val url = async(context + CommonPool) { apps.apps.getOAuthUrl(appRegistration.await()!!.clientId, Scope(Scope.Name.ALL), helper.authCbUrl) }
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url.await())))
        } catch (e: Exception) {
            Timber.e("Login failed: %s", e)
            Toast.makeText(this@MastodonAuthActivity, R.string.login_failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent")

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
                    val uuid = async(context + CommonPool) {
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
                        val mastodonAccount = MastodonAccount(
                                instanceName = instance,
                                userName = account.userName,
                                accessToken = accessToken.accessToken
                        )
                        helper.storeAccount(mastodonAccount)
                        SnsTabRepository(helper).addDefaultTabsFrom(mastodonAccount)
                        return@async mastodonAccount.uuid
                    }
                    val intent = Intent()
                    intent.putExtra(INTENT_ACCOUNT_UUID, uuid.await())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            } else {
                // User canceled!
            }
        } else {
            // other
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        // TODO: アカウント追加の場合はアプリのメイン画面に戻れるようにする
        moveTaskToBack(true)
    }
}

