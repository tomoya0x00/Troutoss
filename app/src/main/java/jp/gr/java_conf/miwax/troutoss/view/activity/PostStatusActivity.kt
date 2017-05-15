package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.model.entity.AccountType

class PostStatusActivity : AppCompatActivity() {

    val accountType: AccountType by lazy {
        intent.extras.getString(EXTRA_ACCOUNT_TYPE)?.let { AccountType.valueOf(it) } ?: AccountType.UNKNOWN
    }

    val accountUuid: String by lazy { intent.extras.getString(EXTRA_ACCOUNT_UUID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_status)
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
