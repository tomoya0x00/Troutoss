package jp.gr.java_conf.miwax.troutoss.model

import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.rx.RxApps
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import io.realm.Realm
import jp.gr.java_conf.miwax.troutoss.App.Companion.appContext
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.extension.OkHttpClientBuilderWithTimeout
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAppRegistration

/**
 * Created by Tomoya Miwa on 2017/04/23.
 * Mastodon用のヘルパー
 */

class MastodonHelper {

    val authCbUrl: String by lazy {
        appContext.getString(R.string.cb_scheme) + "://" + appContext.getString(R.string.mastodon_cb_host) + "/"
    }

    /**
     * アクセストークンセット済みのMastodonクライアント取得。
     */
    fun createAuthedClientOf(account: MastodonAccount): MastodonClient? =
            MastodonClient.Builder(account.instanceName, OkHttpClientBuilderWithTimeout(), Gson())
                    .accessToken(account.accessToken).build()

    /**
     * アクセストークンセット済みのMastodonクライアント取得。
     */
    fun createAuthedClientOf(uuid: String): MastodonClient? =
            loadAccountOf(uuid)?.let { account ->
                createAuthedClientOf(account)
            }

    /**
     * アクセストークンセット済みのMastodonクライアント取得。
     */
    fun createAuthedClientOf(instanceName: String, userName: String): MastodonClient? =
            loadAccountOf(instanceName, userName)?.let {
                MastodonClient.Builder(instanceName, OkHttpClientBuilderWithTimeout(), Gson())
                        .accessToken(it.accessToken).build()
            }

    fun registerAppIfNeededTo(instanceName: String,
                              client: MastodonClient = MastodonClient.Builder(instanceName, OkHttpClientBuilderWithTimeout(), Gson()).build()):
            Single<MastodonAppRegistration?> {
        require(instanceName.isNotEmpty()) { "instanceName is empty!" }

        if (hasAppRegistrationOf(instanceName)) {
            return Single.just(loadAppRegistrationOf(instanceName))
        }

    //    val client: MastodonClient = MastodonClient.Builder(instanceName, OkHttpClientBuilderWithTimeout(), Gson()).build()
        val apps = RxApps(client)

        return apps.createApp(
                clientName = appContext.getString(R.string.app_name),
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
     * 指定したインスタンスのアカウント数を取得。
     */
    fun countAccountOf(instanceName: String): Long =
            Realm.getDefaultInstance().use { realm ->
                realm.where(MastodonAccount::class.java)
                        .equalTo(MastodonAccount::instanceName.name, instanceName)
                        .count()
            }

    /**
     * 指定したインスタンスのアカウントを保持しているか確認。
     */
    fun hasAccountOf(instanceName: String): Boolean =
            countAccountOf(instanceName) > 0

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
    fun clearAccountOf(uuid: String) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                it.where(MastodonAccount::class.java)
                        .equalTo(MastodonAccount::uuid.name, uuid)
                        .findAll()
                        .deleteAllFromRealm()
            }
        }
    }
}
