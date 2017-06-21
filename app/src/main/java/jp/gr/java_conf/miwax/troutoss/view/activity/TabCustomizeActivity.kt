package jp.gr.java_conf.miwax.troutoss.view.activity

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityTabCusomizeBinding
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.adapter.TabDragAdapter
import jp.gr.java_conf.miwax.troutoss.view.dialog.TabMenuDialog
import jp.gr.java_conf.miwax.troutoss.viewmodel.TabCustomizeViewModel


class TabCustomizeActivity : AppCompatActivity() {

    lateinit private var binding: ActivityTabCusomizeBinding
    lateinit private var viewModel: TabCustomizeViewModel

    private val REQUEST_SELECT_TAB_TYPE = 100
    private var adapter: TabDragAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tab_cusomize)
        adapter = object : TabDragAdapter(binding.tabs) {
            override fun onClickTab(position: Int, tab: SnsTab, name: CharSequence) {
                TabMenuDialog(this@TabCustomizeActivity, name).show()
                        .doOnNext { viewModel.onSelectAction(it, position, tab) }
                        .subscribe()
            }
        }
        viewModel = TabCustomizeViewModel(adapter!!)
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
                tab?.let { adapter?.add(it) }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tab_customize_appbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
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
}
