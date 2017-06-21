package jp.gr.java_conf.miwax.troutoss.view.activity

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityTabCusomizeBinding
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.adapter.TabDragAdapter
import jp.gr.java_conf.miwax.troutoss.view.dialog.TabMenuDialog
import jp.gr.java_conf.miwax.troutoss.viewmodel.TabCustomizeViewModel


class TabCustomizeActivity : AppCompatActivity() {

    lateinit private var binding: ActivityTabCusomizeBinding
    lateinit private var viewModel: TabCustomizeViewModel
    lateinit private var adapter: TabDragAdapter
    lateinit var before: Array<SnsTab>

    private val REQUEST_SELECT_TAB_TYPE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabs = savedInstanceState?.let { it.getParcelableArray(SAVE_TAB_LIST)?.toMutableList() }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tab_cusomize)
        adapter = object : TabDragAdapter(binding.tabs, tabs?.let { it as? MutableList<SnsTab> }) {
            override fun onClickTab(position: Int, tab: SnsTab, name: CharSequence) {
                TabMenuDialog(this@TabCustomizeActivity, name).show()
                        .doOnNext { viewModel.onSelectAction(it, position, tab) }
                        .subscribe()
            }
        }
        before = adapter.getReIndexedTabs().map { it.clone() }.toTypedArray()
        viewModel = TabCustomizeViewModel(adapter)
        binding.viewModel = viewModel

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        binding.fab.setOnClickListener {
            startActivityForResult(Intent(this, SelectTabTypeActivity::class.java), REQUEST_SELECT_TAB_TYPE)
        }

        binding.tabs.apply {
            layoutManager = LinearLayoutManager(this@TabCustomizeActivity)
            adapter = this@TabCustomizeActivity.adapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == REQUEST_SELECT_TAB_TYPE && resultCode == Activity.RESULT_OK && data != null -> {
                val tab = data.let { SelectTabTypeActivity.extractSnsTab(it) }
                tab?.let { adapter.add(it) }
            }
        }
    }

    private fun hasChanged(): Boolean {
        return before.contentDeepEquals(adapter.getReIndexedTabs().toTypedArray())
    }

    private fun confirmIfChanged(block: () -> Unit) {
        if (!hasChanged()) {
            block()
            return
        }

        MaterialDialog.Builder(this)
                .content(R.string.confirm_close_tab_customize)
                .positiveText(R.string.close)
                .negativeText(R.string.cancel)
                .onPositive { _, _ -> block() }
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tab_customize_appbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                confirmIfChanged(this::finish)
                true
            }
            R.id.ok -> {
                viewModel.onClickOk()
                finish()
                true
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        confirmIfChanged(this::finish)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putParcelableArray(SAVE_TAB_LIST, adapter.tabs.toTypedArray())
        }
        super.onSaveInstanceState(outState)
    }

    companion object {
        private val SAVE_TAB_LIST = "save_tab_list"
    }
}
