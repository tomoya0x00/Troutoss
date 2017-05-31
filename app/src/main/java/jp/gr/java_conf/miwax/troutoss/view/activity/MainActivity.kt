package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import io.realm.Realm
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityMainBinding
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.view.adapter.SnsTabAdapter

class MainActivity : AppCompatActivity() {

    lateinit private var binding: ActivityMainBinding
    private val helper: MastodonHelper by lazy { MastodonHelper() }
    private val realm: Realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbar)
        val adapter = SnsTabAdapter(supportFragmentManager, realm)
        binding.container.adapter = adapter
        binding.tabs.setupWithViewPager(binding.container)

        binding.fab.setOnClickListener { _ ->
            val (accountType, accountUuid) = (adapter.getAccount(binding.container.currentItem))
            PostStatusActivity.startActivity(this, accountType, accountUuid)
        }

        val drawerToggle = ActionBarDrawerToggle(this,
                binding.drawer, binding.toolbar,
                R.string.drawer_open, R.string.drawer_close)
        binding.drawer.addDrawerListener(drawerToggle)

        // アカウントを持っていない場合、Mastodon認証画面を出す
        if (!helper.hasAccount()) {
            startActivity(Intent(this, MastodonAuthActivity::class.java))
        }
    }

    override fun onDestroy() {
        binding.container.adapter = null
        realm.close()
        super.onDestroy()
    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
