package jp.gr.java_conf.miwax.troutoss.view.activity

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivitySelectTabTypeBinding
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.dialog.MastodonAccountDialog

class SelectTabTypeActivity : AppCompatActivity() {

    lateinit var binding: ActivitySelectTabTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_tab_type)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.select_add_tab_type)
        }

        val fragment = fragmentManager.findFragmentByTag(SettingsFragment::class.java.name) ?:
                SettingsFragment()
        fragmentManager.beginTransaction()
                .replace(R.id.pref_content, fragment)
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return true
        }
        return false
    }

    class SettingsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.select_tab_type)

            preferenceManager.findPreference(getString(R.string.pref_key_add_mastodon_home))
                    .setOnPreferenceClickListener { onClickAddMastodonTab(SnsTab.TabType.MASTODON_HOME);true }
            preferenceManager.findPreference(getString(R.string.pref_key_add_mastodon_local))
                    .setOnPreferenceClickListener { onClickAddMastodonTab(SnsTab.TabType.MASTODON_LOCAL);true }
            preferenceManager.findPreference(getString(R.string.pref_key_add_mastodon_federated))
                    .setOnPreferenceClickListener { onClickAddMastodonTab(SnsTab.TabType.MASTODON_FEDERATED);true }
            preferenceManager.findPreference(getString(R.string.pref_key_add_mastodon_notifications))
                    .setOnPreferenceClickListener { onClickAddMastodonTab(SnsTab.TabType.MASTODON_NOTIFICATIONS);true }
            preferenceManager.findPreference(getString(R.string.pref_key_add_mastodon_favourite))
                    .setOnPreferenceClickListener { onClickAddMastodonTab(SnsTab.TabType.MASTODON_FAVOURITES);true }
        }

        private fun onClickAddMastodonTab(type: SnsTab.TabType) {
            MastodonAccountDialog(activity).show()
                    .subscribe {
                        val intent = Intent().apply {
                            putExtra(SelectTabTypeActivity.EXTRA_SNS_TAB, SnsTab(
                                    type = type,
                                    accountUuid = it.uuid
                            ))
                        }
                        activity.run {
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }
        }

    }

    companion object {
        val EXTRA_SNS_TAB = "extra_sns_tab"

        fun extractSnsTab(intent: Intent): SnsTab? {
            return intent.getParcelableExtra<SnsTab>(EXTRA_SNS_TAB)
        }
    }
}
