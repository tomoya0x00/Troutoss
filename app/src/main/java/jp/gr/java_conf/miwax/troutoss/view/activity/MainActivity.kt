package jp.gr.java_conf.miwax.troutoss.view.activity

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import jp.gr.java_conf.miwax.troutoss.BuildConfig
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityMainBinding
import jp.gr.java_conf.miwax.troutoss.messenger.*
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.SnsTabRepository
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.view.adapter.DrawerAccountAdapter
import jp.gr.java_conf.miwax.troutoss.view.adapter.SnsTabAdapter
import jp.gr.java_conf.miwax.troutoss.view.fragment.MastodonBaseFragment
import jp.gr.java_conf.miwax.troutoss.viewmodel.MainViewModel
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    lateinit private var binding: ActivityMainBinding
    lateinit private var viewModel: MainViewModel
    lateinit private var adapter: SnsTabAdapter

    private val helper: MastodonHelper by lazy { MastodonHelper() }
    private val tabRepository: SnsTabRepository by lazy { SnsTabRepository(helper) }
    private val realm: Realm = Realm.getDefaultInstance()
    private val disposables = CompositeDisposable()
    private val drawerAccountView: RecyclerView by lazy { findViewById(R.id.account_view) as RecyclerView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        viewModel = MainViewModel()
        binding.viewModel = viewModel

        setSupportActionBar(binding.toolbar)
        adapter = SnsTabAdapter(supportFragmentManager, realm)
        binding.container.adapter = adapter
        binding.tabs.setupWithViewPager(binding.container)
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                val fragment = adapter.findFragmentAt(binding.container, tab.position)
                if (fragment is MastodonBaseFragment) {
                    fragment.onReselected()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {}
        })

        binding.fab.setOnClickListener { _ ->
            val (accountType, accountUuid) = (adapter.getAccount(binding.container.currentItem))
            if (accountType != AccountType.UNKNOWN) {
                PostStatusActivity.startActivity(this, accountType, accountUuid)
            }
        }

        setupDrawer()

        MobileAds.initialize(applicationContext, getString(R.string.addAppId))
        val adBuilder = AdRequest.Builder()
        if (BuildConfig.DEBUG) {
            adBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("3FB68A2D733507E23491D9D48E953313")
        }
        binding.adBanner.loadAd(adBuilder.build())

        // アカウントを持っていない場合、Mastodon認証画面を出す
        if (!helper.hasAccount()) {
            startActivity(Intent(this, MastodonAuthActivity::class.java))
        }
    }

    private fun setupDrawer() {
        val drawerToggle = ActionBarDrawerToggle(this,
                binding.drawer, binding.toolbar,
                R.string.drawer_open, R.string.drawer_close)
        binding.drawer.addDrawerListener(drawerToggle)

        val adapter = DrawerAccountAdapter(realm)
        disposables.addAll(
                adapter.messenger.register(ShowMastodonAccountSettingsActivityMessage::class.java).doOnNext {
                    Timber.d("received ShowMastodonAccountSettingsActivityMessage")
                    MastodonAccountSettingsActivity.startActivityForResult(this, it.accountUuid, REQUEST_MASTODON_ACCOUNT_SETTINGS)
                }.subscribe(),
                adapter.messenger.register(ShowMastodonNotificationsActivityMessage::class.java).doOnNext {
                    Timber.d("received ShowMastodonNotificationsActivityMessage")
                    MastodonNotificationsActivity.startActivity(this, it.accountUuid)
                }.subscribe(),
                adapter.messenger.register(ShowMastodonTimelineActivityMessage::class.java).doOnNext {
                    Timber.d("received ShowMastodonTimelineActivityMessage")
                    MastodonTimelineActivity.startActivity(this, it.timeline, it.accountUuid, it.option)
                }.subscribe(),
                adapter.messenger.register(CloseDrawerMessage::class.java).doOnNext {
                    Timber.d("received CloseDrawerMessage")
                    binding.drawer.closeDrawer(binding.navigation)
                }.subscribe(),
                viewModel.messenger.register(ShowSettingsActivityMessage::class.java).doOnNext {
                    Timber.d("received ShowSettingsActivityMessage")
                    startActivity(Intent(this, SettingsActivity::class.java))
                }.subscribe(),
                viewModel.messenger.register(ShowAccountAuthActivityMessage::class.java).doOnNext {
                    Timber.d("received ShowAccountAuthActivityMessage")
                    startActivityForResult(Intent(this, MastodonAuthActivity::class.java), REQUEST_MASTODON_AUTH)
                }.subscribe(),
                viewModel.messenger.register(ShowTabCustomizeActivityMessage::class.java).doOnNext {
                    Timber.d("received ShowTabCustomizeActivityMessage")
                    startActivity(Intent(this, TabCustomizeActivity::class.java))
                }.subscribe(), viewModel.messenger.register(CloseDrawerMessage::class.java).doOnNext {
            Timber.d("received CloseDrawerMessage")
            binding.drawer.closeDrawer(binding.navigation)
        }.subscribe()
        )
        drawerAccountView.layoutManager = LinearLayoutManager(this)
        drawerAccountView.adapter = adapter
        drawerAccountView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == REQUEST_MASTODON_ACCOUNT_SETTINGS && resultCode == Activity.RESULT_OK -> {
                // アカウントが無くなった場合、Mastodon認証画面を出す
                if (!helper.hasAccount()) {
                    startActivity(Intent(this, MastodonAuthActivity::class.java))
                }
            }
            requestCode == REQUEST_MASTODON_AUTH && resultCode == Activity.RESULT_OK -> {
                // 追加したアカウントのタブを表示
                data?.getStringExtra(MastodonAuthActivity.EXTRA_ACCOUNT_UUID)?.let { uuid ->
                    val index = tabRepository.findFirstTabIndexOf(uuid)
                    index?.let { binding.tabs.getTabAt(it)?.select() }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                val currentTab = adapter.getSnsTabAt(binding.tabs.selectedTabPosition)
                MastodonSearchActivity.startActivity(this, currentTab)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        binding.container.adapter = null
        drawerAccountView.adapter = null
        realm.close()
        disposables.clear()
        super.onDestroy()
    }

    companion object {

        private val REQUEST_MASTODON_ACCOUNT_SETTINGS = 100
        private val REQUEST_MASTODON_AUTH = REQUEST_MASTODON_ACCOUNT_SETTINGS + 1

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
