package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import io.realm.Realm
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityMastodonNotificationsBinding
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.fragment.MastodonNotificationsFragment

class MastodonNotificationsActivity : AppCompatActivity() {

    lateinit private var binding: ActivityMastodonNotificationsBinding

    private val realm = Realm.getDefaultInstance()
    private val accountUuid: String by lazy { intent.extras.getString(EXTRA_ACCOUNT_UUID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mastodon_notifications)

        val account = MastodonHelper().loadAccountOf(accountUuid)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.notifications)
            account?.let { subtitle = it.userNameWithInstance }
        }

        val fragment = supportFragmentManager.findFragmentByTag(MastodonNotificationsFragment::class.java.name) ?: createFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    private fun createFragment(): MastodonNotificationsFragment {
        val options = realm.where(SnsTab::class.java)
                .equalTo(SnsTab::accountUuid.name, accountUuid)
                .findFirst()?.option ?: ""

        return MastodonNotificationsFragment.newInstance(accountUuid, options)
    }

    override fun onDestroy() {
        realm.close()
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
        private val EXTRA_ACCOUNT_UUID = "account_uuid"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        fun startActivity(context: Context, accountUuid: String) {
            val intent = Intent(context, MastodonNotificationsActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_UUID, accountUuid)
            context.startActivity(intent)
        }
    }
}
