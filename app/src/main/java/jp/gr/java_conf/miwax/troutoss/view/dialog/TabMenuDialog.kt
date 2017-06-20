package jp.gr.java_conf.miwax.troutoss.view.dialog

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import jp.gr.java_conf.miwax.troutoss.R

/**
 * Created by Tomoya Miwa on 2017/06/20.
 * タブカスタマイズのメニューダイアログ
 */

class TabMenuDialog(context: Context, tabName: CharSequence) {

    enum class Action {
        DELETE,
        RENAME,
        RESET_NAME;

        var arg: String? = null
    }

    private val processor =  BehaviorProcessor.create<Action>()
    private val builder: MaterialDialog

    init {
        val adapter = MaterialSimpleListAdapter({ d, _, item ->
            val tag = item.tag
            if (tag is String) {
                processor.onNext(Action.valueOf(tag))
            }
            d.dismiss()
        })

        val background = R.color.dialog_background
        val padding = 4

        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(R.string.delete)
                .backgroundColorRes(background)
                .tag(Action.DELETE.name)
                .build())
        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(R.string.rename)
                .backgroundColorRes(background)
                .tag(Action.RENAME.name)
                .build())
        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(R.string.reset_name)
                .backgroundColorRes(background)
                .tag(Action.RESET_NAME.name)
                .build())

        builder = MaterialDialog.Builder(context)
                .adapter(adapter, null)
                .title(tabName)
                .negativeText(R.string.cancel)
                .dismissListener { processor.onComplete() }
                .autoDismiss(true)
                .build()
    }

    fun show(): Flowable<Action> {
        builder.show()
        return processor
    }
}
