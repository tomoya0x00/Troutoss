package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityPostStatusBinding
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType

class PostStatusActivity : AppCompatActivity() {

    lateinit private var binding: ActivityPostStatusBinding

    private val accountType: AccountType by lazy {
        intent.extras.getString(EXTRA_ACCOUNT_TYPE)?.let { AccountType.valueOf(it) } ?: AccountType.UNKNOWN
    }

    private val accountUuid: String by lazy { intent.extras.getString(EXTRA_ACCOUNT_UUID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_status)

         supportActionBar?.let {
            it.displayOptions = ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
            it.setDisplayHomeAsUpEnabled(true)
        }
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

        fun startActivity(context: Context, accountType: AccountType, accountUuid: String) {
            val intent = Intent(context, PostStatusActivity::class.java)
            intent.putExtra(PostStatusActivity.EXTRA_ACCOUNT_TYPE, accountType.toString())
            intent.putExtra(PostStatusActivity.EXTRA_ACCOUNT_UUID, accountUuid)
            context.startActivity(intent)
        }
    }
}
