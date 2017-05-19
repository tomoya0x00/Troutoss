package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
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
        val adapter = SnsTabAdapter(supportFragmentManager, realm, this)
        binding.container.adapter = adapter
        binding.tabs.setupWithViewPager(binding.container)

        binding.fab.setOnClickListener { _ ->
            val (accountType, accountUuid) = (adapter.getAccount(binding.container.currentItem))
            PostStatusActivity.startActivity(this, accountType, accountUuid)
        }

        // アカウントを持っていない場合、Mastodon認証画面を出す
        if (!helper.hasAccount()) {
            startActivity(Intent(this, MastodonAuthActivity::class.java))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        binding.container.adapter = null
        realm.close()
        super.onDestroy()
    }
}
