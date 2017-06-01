package jp.gr.java_conf.miwax.troutoss.view.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragment = fragmentManager.findFragmentByTag(SettingsFragment::class.java.name) ?: SettingsFragment()
        fragmentManager.beginTransaction()
                .replace(R.id.pref_content, fragment)
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    class SettingsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.settings)
        }
    }
}
