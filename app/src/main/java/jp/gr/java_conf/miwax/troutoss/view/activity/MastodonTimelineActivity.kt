package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import io.realm.Realm
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityMastodonTimelineBinding
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonTimelineAdapter
import jp.gr.java_conf.miwax.troutoss.view.fragment.MastodonTimelineFragment

class MastodonTimelineActivity : AppCompatActivity() {

    lateinit private var binding: ActivityMastodonTimelineBinding

    private val realm = Realm.getDefaultInstance()
    private val timeline: MastodonTimelineAdapter.Timeline by lazy {
        intent.extras.getString(EXTRA_TIMELINE)?.let {
            MastodonTimelineAdapter.Timeline.valueOf(it)
        } ?: MastodonTimelineAdapter.Timeline.HOME
    }
    private val accountUuid: String by lazy { intent.extras.getString(EXTRA_ACCOUNT_UUID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mastodon_timeline)

        val titleRes = when (timeline) {
            MastodonTimelineAdapter.Timeline.HOME -> R.string.home
            MastodonTimelineAdapter.Timeline.LOCAL -> R.string.local
            MastodonTimelineAdapter.Timeline.FEDERATED -> R.string.federated
            MastodonTimelineAdapter.Timeline.FAVOURITES -> R.string.favourite
        }
        val account = MastodonHelper().loadAccountOf(accountUuid)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(titleRes)
            account?.let { subtitle = it.userNameWithInstance }
        }

        val fragment = supportFragmentManager.findFragmentByTag(MastodonTimelineFragment::class.java.name) ?: createFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    private fun createFragment(): MastodonTimelineFragment {
        val options = realm.where(SnsTab::class.java)
                .equalTo(SnsTab::accountUuid.name, accountUuid)
                .findFirst()?.option ?: ""

        return MastodonTimelineFragment.newInstance(timeline, accountUuid, options)
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    companion object {
        private val EXTRA_TIMELINE = "timeline"
        private val EXTRA_ACCOUNT_UUID = "account_uuid"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        fun startActivity(context: Context, timeline: MastodonTimelineAdapter.Timeline, accountUuid: String) {
            val intent = Intent(context, MastodonTimelineActivity::class.java)
            intent.putExtra(EXTRA_TIMELINE, timeline.toString())
            intent.putExtra(EXTRA_ACCOUNT_UUID, accountUuid)
            context.startActivity(intent)
        }
    }
}
