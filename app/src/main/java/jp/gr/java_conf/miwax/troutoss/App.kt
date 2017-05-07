package jp.gr.java_conf.miwax.troutoss

import android.app.Application
import com.deploygate.sdk.DeployGate
import com.jakewharton.threetenabp.AndroidThreeTen
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

/**
 * Created by Tomoya Miwa on 2017/04/23.
 * App class
 */

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            DeployGate.install(this)

        } else {
            // TODO: Firebaseにログを送信
            Timber.plant(Timber.DebugTree())
        }

        // Realmデフォルト設定
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded() // TODO: リリース時に外す
                .schemaVersion(1)
                .build()
        Realm.setDefaultConfiguration(realmConfig)
    }
}
