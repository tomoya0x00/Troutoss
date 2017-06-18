package jp.gr.java_conf.miwax.troutoss.view.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityTabCusomizeBinding
import jp.gr.java_conf.miwax.troutoss.view.adapter.TabDragAdapter
import jp.gr.java_conf.miwax.troutoss.viewmodel.TabCustomizeViewModel


class TabCustomizeActivity : AppCompatActivity() {

    lateinit private var binding: ActivityTabCusomizeBinding
    lateinit private var viewModel: TabCustomizeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tab_cusomize)
        val adapter = TabDragAdapter(binding.tabs)
        viewModel = TabCustomizeViewModel(adapter)
        binding.viewModel = viewModel

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        binding.tabs.apply {
            layoutManager = LinearLayoutManager(this@TabCustomizeActivity)
            setAdapter(adapter)
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
