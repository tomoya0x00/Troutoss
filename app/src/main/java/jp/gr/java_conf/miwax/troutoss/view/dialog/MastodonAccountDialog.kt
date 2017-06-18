package jp.gr.java_conf.miwax.troutoss.view.dialog

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount

/**
 * Created by Tomoya Miwa on 2017/06/19.
 * Mastodonのアカウント選択用ダイアログ
 */

class MastodonAccountDialog(context: Context) {

    private val helper = MastodonHelper()
    private val processor = BehaviorProcessor.create<MastodonAccount>()
    private val dialog: MaterialDialog

    init {
        val adapter = MaterialSimpleListAdapter({ d, _, item ->
            val tag = item.tag
            if (tag is MastodonAccount) {
                processor.onNext(tag)
            }
            d.dismiss()
        })

        val background = R.color.dialog_background
        val padding = 4

        helper.loadAllAccount().forEach {
            adapter.add(MaterialSimpleListItem.Builder(context)
                    .content(it.userNameWithInstance)
                    .backgroundColorRes(background)
                    .tag(it)
                    .build())
        }

        dialog = MaterialDialog.Builder(context)
                .title(R.string.select_account)
                .adapter(adapter, null)
                .negativeText(R.string.cancel)
                .dismissListener { processor.onComplete() }
                .autoDismiss(true)
                .build()
    }

    fun show(): Flowable<MastodonAccount> {
        dialog.show()
        return processor
    }
}

