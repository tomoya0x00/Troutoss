package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityMastodonSearchBinding
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.viewmodel.MastodonSearchViewModel
import timber.log.Timber

class MastodonSearchActivity : AppCompatActivity() {

    lateinit var binding: ActivityMastodonSearchBinding
    lateinit var viewModel: MastodonSearchViewModel

    private val helper = MastodonHelper()

    private val accountType: AccountType by lazy {
        intent.extras.getString(EXTRA_ACCOUNT_TYPE)?.let { AccountType.valueOf(it) } ?: AccountType.UNKNOWN
    }

    private val accountUuid: String by lazy { intent.extras.getString(EXTRA_ACCOUNT_UUID) }

    private val account: MastodonAccount? by lazy{accountUuid.let { helper.loadAccountOf(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_mastodon_search)
        viewModel = MastodonSearchViewModel(accountUuid)
        binding.viewModel = viewModel
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search, menu)

        val searchMenu = menu.findItem(R.id.action_search)
        MenuItemCompat.setOnActionExpandListener(searchMenu, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                onBackPressed()
                return false
            }
        })
        val searchView = searchMenu.actionView as SearchView
        searchView.apply {
            queryHint = account?.let {
                getString(R.string.mastodon_search_hint,it.instanceName)
            } ?: getString(R.string.search)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    Timber.d("onQueryTextSubmit : $query")
                    viewModel.onQueryTextSubmit(query)
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    Timber.d("onQueryTextChange : $query")
                    viewModel.onQueryTextChange(query)
                    return true
                }
            })
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        MenuItemCompat.expandActionView(menu.findItem(R.id.action_search))
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    companion object {

        private val EXTRA_ACCOUNT_TYPE = "account_type"
        private val EXTRA_ACCOUNT_UUID = "account_uuid"

        fun startActivity(context: Context, tab: SnsTab) {
            val intent = Intent(context, MastodonSearchActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_TYPE, tab.type.accountType.toString())
            intent.putExtra(EXTRA_ACCOUNT_UUID, tab.accountUuid)

            context.startActivity(intent)
        }
    }
}
