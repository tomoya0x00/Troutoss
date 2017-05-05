package jp.gr.java_conf.miwax.troutoss.model

import android.content.Context
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.rx.RxApps
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import io.realm.Realm
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAppRegistration
import okhttp3.OkHttpClient

/**
 * Created by Tomoya Miwa on 2017/04/23.
 * Mastodon用のヘルパー
 */

class MastodonHelper(val context: Context) {

    val authCbUrl: String by lazy {
        context.getString(R.string.cb_scheme) + "://" + context.getString(R.string.mastodon_cb_host) + "/"
    }

    /**
     * アクセストークンセット済みのMastodonクライアント取得。
     */
    fun createAuthedClientOf(account: MastodonAccount): MastodonClient? =
            MastodonClient(account.instanceName, OkHttpClient(), Gson(), account.accessToken)

    /**
     * アクセストークンセット済みのMastodonクライアント取得。
     */
    fun createAuthedClientOf(uuid: String): MastodonClient? =
            loadAccountOf(uuid)?.let { account ->
                MastodonClient(account.instanceName, OkHttpClient(), Gson(), account.accessToken)
            }

    /**
     * アクセストークンセット済みのMastodonクライアント取得。
     */
    fun createAuthedClientOf(instanceName: String, userName: String): MastodonClient? =
            loadAccountOf(instanceName, userName)?.let { MastodonClient(instanceName, OkHttpClient(), Gson(), it.accessToken) }

    fun registerAppIfNeededTo(instanceName: String): Single<MastodonAppRegistration?> {
        require(instanceName.isNotEmpty()) { "instanceName is empty!" }

        if (hasAppRegistrationOf(instanceName)) {
            return Single.just(loadAppRegistrationOf(instanceName))
        }

        val client: MastodonClient = MastodonClient(instanceName, OkHttpClient(), Gson())
        val apps = RxApps(client)

        return apps.createApp(
                clientName = context.getString(R.string.app_name),
                redirectUris = authCbUrl,
                scope = Scope(Scope.Name.ALL))
                .subscribeOn(io())
                .map { MastodonAppRegistration(instanceName = it.instanceName, clientId = it.clientId, clientSecret = it.clientSecret) }
                .doOnSuccess { storeAppRegistration(it) }
    }

    /**
     * アプリがインスタンスに登録済みかチェック。
     *
     */
    fun hasAppRegistrationOf(instanceName: String): Boolean =
            Realm.getDefaultInstance().use { realm ->
                realm.where(MastodonAppRegistration::class.java)
                        .equalTo(MastodonAppRegistration::instanceName.name, instanceName)
                        .count() > 0
            }

    /**
     * アプリ登録情報の取得。
     */
    fun loadAppRegistrationOf(instanceName: String): MastodonAppRegistration? =
            Realm.getDefaultInstance().use { realm ->
                realm.where(MastodonAppRegistration::class.java)
                        .equalTo(MastodonAppRegistration::instanceName.name, instanceName)
                        .findFirst()?.let { realm.copyFromRealm(it) }
            }

    /**
     * アプリ登録情報の保存。
     */
    fun storeAppRegistration(registration: MastodonAppRegistration) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                realm.copyToRealm(registration)
            }
        }
    }

    /**
     * アカウントを保持しているか確認。
     */
    fun hasAccount(): Boolean =
            Realm.getDefaultInstance().use { realm ->
                realm.where(MastodonAccount::class.java).count() > 0
            }

    /**
     * 指定したインスタンスのアカウントを保持しているか確認。
     */
    fun hasAccountOf(instanceName: String): Boolean =
            Realm.getDefaultInstance().use { realm ->
                realm.where(MastodonAccount::class.java)
                        .equalTo(MastodonAccount::instanceName.name, instanceName)
                        .count() > 0
            }

    /**
     * 全アカウント情報の取得。
     */
    fun loadAllAccount(): List<MastodonAccount> =
            Realm.getDefaultInstance().use { realm ->
                realm.where(MastodonAccount::class.java)
                        .findAll().let { realm.copyFromRealm(it) }
            }

    /**
     * アカウント情報の取得。
     */
    fun loadAccountOf(instanceName: String, userName: String): MastodonAccount? =
            Realm.getDefaultInstance().use { realm ->
                realm.where(MastodonAccount::class.java)
                        .equalTo(MastodonAccount::instanceName.name, instanceName)
                        .equalTo(MastodonAccount::userName.name, userName)
                        .findFirst()?.let { realm.copyFromRealm(it) }
            }

    /**
     * アカウント情報の取得。
     */
    fun loadAccountOf(uuid: String): MastodonAccount? =
            Realm.getDefaultInstance().use { realm ->
                realm.where(MastodonAccount::class.java)
                        .equalTo(MastodonAccount::uuid.name, uuid)
                        .findFirst()?.let { realm.copyFromRealm(it) }
            }

    /**
     *  アカウント情報の保存。
     */
    fun storeAccount(account: MastodonAccount) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                it.copyToRealm(account)
            }
        }
    }

    /**
     * アカウント情報の消去。
     */
    fun clearAccountOf(instanceName: String, userName: String) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                it.where(MastodonAccount::class.java)
                        .equalTo(MastodonAccount::instanceName.name, instanceName)
                        .equalTo(MastodonAccount::userName.name, userName)
                        .findFirst()?.deleteFromRealm()
            }
        }
    }
}