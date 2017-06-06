package jp.gr.java_conf.miwax.troutoss.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceFragment
import android.support.annotation.StringRes
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import android.widget.Toast
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityMastodonAccountSettingsBinding
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.SnsTabRepository
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount

class MastodonAccountSettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivityMastodonAccountSettingsBinding

    private val accountUuid: String by lazy { intent.extras.getString(EXTRA_ACCOUNT_UUID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mastodon_account_settings)

        val account = MastodonHelper().loadAccountOf(accountUuid)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.account_settings)
            account?.let { subtitle = it.userNameWithInstance }
        }


        val fragment = fragmentManager.findFragmentByTag(SettingsFragment::class.java.name) ?:
                SettingsFragment.newInstance(accountUuid)
        fragmentManager.beginTransaction()
                .replace(R.id.pref_content, fragment)
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_OK)
            finish()
            return true
        }
        return false
    }

    class SettingsFragment : PreferenceFragment() {

        private val handler = Handler(Looper.getMainLooper())
        private val accountUuid: String by lazy { arguments.getString(ARG_ACCOUNT_UUID) }
        private val helper: MastodonHelper by lazy { MastodonHelper() }
        private val account: MastodonAccount? by lazy { helper.loadAccountOf(accountUuid) }

        private val tabsIntent: CustomTabsIntent by lazy {
            CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                    .setStartAnimations(activity, R.anim.slide_in_right, R.anim.slide_out_left)
                    .setExitAnimations(activity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .build()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.mastodon_account_settings)

            account?.let {
                preferenceManager.findPreference(getString(R.string.pref_key_mastodon_muted_users))
                        .setOnPreferenceClickListener { _ -> tabsIntent.launchUrl(activity, Uri.parse(it.mutesUrl));true }
                preferenceManager.findPreference(getString(R.string.pref_key_mastodon_blocked_users))
                        .setOnPreferenceClickListener { _ -> tabsIntent.launchUrl(activity, Uri.parse(it.blocksUrl));true }
                preferenceManager.findPreference(getString(R.string.pref_key_mastodon_logout))
                        .setOnPreferenceClickListener { _ ->
                            deleteAccount()
                            showToast(R.string.logout_and_delete_tabs, Toast.LENGTH_SHORT)
                            true
                        }
            }
        }

        private fun deleteAccount() {
            SnsTabRepository(helper).deleteTabsOf(accountUuid)
            helper.clearAccountOf(accountUuid)
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }


        private fun showToast(@StringRes resId: Int, duration: Int) {
            handler.post {
                Toast.makeText(activity, resId, duration).show()
            }
        }

        companion object {
            private val ARG_ACCOUNT_UUID = "account_uuid"

            fun newInstance(accountUuid: String): SettingsFragment {
                val fragment = SettingsFragment()
                val args = Bundle()
                args.putString(ARG_ACCOUNT_UUID, accountUuid)
                fragment.arguments = args
                return fragment
            }
        }
    }

    companion object {
        private val EXTRA_ACCOUNT_UUID = "account_uuid"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        fun startActivity(context: Context, accountUuid: String) {
            val intent = Intent(context, MastodonAccountSettingsActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_UUID, accountUuid)
            context.startActivity(intent)
        }

        fun startActivityForResult(activity: Activity, accountUuid: String, requestCode: Int) {
            val intent = Intent(activity, MastodonAccountSettingsActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_UUID, accountUuid)
            activity.startActivityForResult(intent, requestCode)
        }
    }
}