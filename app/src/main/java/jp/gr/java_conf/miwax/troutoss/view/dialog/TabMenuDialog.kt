package jp.gr.java_conf.miwax.troutoss.view.dialog

import android.content.Context
import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.processors.BehaviorProcessor
import jp.gr.java_conf.miwax.troutoss.R


/**
 * Created by Tomoya Miwa on 2017/06/20.
 * タブカスタマイズのメニューダイアログ
 */

class TabMenuDialog(private val context: Context, private val tabName: CharSequence) {

    enum class Action {
        DELETE,
        RENAME,
        RESET_NAME;

        var arg: String? = null
    }

    private val processor = BehaviorProcessor.create<Action>()
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

    private fun editTabName(): Flowable<CharSequence> {
        return Flowable.create({ emitter: FlowableEmitter<CharSequence> ->
            MaterialDialog.Builder(context)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .onNegative { _, _ -> emitter.onComplete() }
                    .input("", tabName, { _, input ->
                        val newName = input ?: ""
                        emitter.onNext(newName)
                        emitter.onComplete()
                    }).show()

        }, BackpressureStrategy.LATEST)
    }

    fun show(): Flowable<Action> {
        builder.show()
        return processor.flatMap { action ->
            when(action) {
                Action.RENAME -> editTabName().map { action.apply { arg = it.toString() } }
                else -> Flowable.just(action)
            }
        }
    }
}
