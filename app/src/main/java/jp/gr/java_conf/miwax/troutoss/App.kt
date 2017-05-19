package jp.gr.java_conf.miwax.troutoss

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.deploygate.sdk.DeployGate
import com.google.firebase.crash.FirebaseCrash
import com.jakewharton.threetenabp.AndroidThreeTen
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

/**
 * Created by Tomoya Miwa on 2017/04/23.
 * App class
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        App.app = this

        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            DeployGate.install(this)

        } else {
            Timber.plant(FirebaseTree())
        }

        // Realmデフォルト設定
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded() // TODO: リリース時に外す
                .schemaVersion(1)
                .build()
        Realm.setDefaultConfiguration(realmConfig)
    }

    private class FirebaseTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String?, e: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return
            }

            if (e == null) {
                FirebaseCrash.logcat(priority, tag, message)
            } else {
                FirebaseCrash.report(e)
            }
        }
    }

    companion object {
        lateinit private var app: Application

        val appContext: Context
            get() = app

        val appResources: Resources
            get() = app.resources
    }
}
