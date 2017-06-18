package jp.gr.java_conf.miwax.troutoss.view.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityTabCusomizeBinding
import jp.gr.java_conf.miwax.troutoss.view.adapter.TabDragAdapter


class TabCustomizeActivity : AppCompatActivity() {

    lateinit private var binding: ActivityTabCusomizeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tab_cusomize)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tabs.apply {
            layoutManager = LinearLayoutManager(this@TabCustomizeActivity)
            adapter = TabDragAdapter(binding.tabs)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }
}
