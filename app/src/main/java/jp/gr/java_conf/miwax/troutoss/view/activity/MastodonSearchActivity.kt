package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import timber.log.Timber

class MastodonSearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mastodon_search)
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
            queryHint = "mstdn.jpから検索"
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    Timber.d("onQueryTextSubmit : $query")
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    Timber.d("onQueryTextChange : $query")
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
        fun startActivity(context: Context, tab: SnsTab?) {
            val intent = Intent(context, MastodonSearchActivity::class.java)
            context.startActivity(intent)
        }
    }
}
