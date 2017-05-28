package jp.gr.java_conf.miwax.troutoss.view.dialog

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import jp.gr.java_conf.miwax.troutoss.R

/**
 * Created by Tomoya Miwa on 2017/05/27.
 * MastodonのVisibility選択用ダイアログ
 */

class MastodonVisibilityDialog(context: Context) {

    enum class Result {
        PUBLIC,
        UNLISTED,
        PRIVATE,
        DIRECT
    }

    private val processor =  BehaviorProcessor.create<Result>()
    private val dialog: MaterialDialog

    init {
        val adapter = MaterialSimpleListAdapter({ d, _, item ->
            val tag = item.tag
            if (tag is String) {
                processor.onNext(Result.valueOf(tag))
            }
            d.dismiss()
        })

        val background = R.color.dialog_background
        val padding = 4

        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(R.string.visibility_public)
                .icon(R.drawable.public_earth)
                .iconPaddingDp(padding)
                .backgroundColorRes(background)
                .tag(Result.PUBLIC.name)
                .build())
        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(R.string.visibility_unlisted)
                .icon(R.drawable.lock_open)
                .iconPaddingDp(padding)
                .backgroundColorRes(background)
                .tag(Result.UNLISTED.name)
                .build())
        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(R.string.visibility_private)
                .icon(R.drawable.lock_close)
                .iconPaddingDp(padding)
                .backgroundColorRes(background)
                .tag(Result.PRIVATE.name)
                .build())
        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(R.string.visibility_direct)
                .icon(R.drawable.direct)
                .iconPaddingDp(padding)
                .backgroundColorRes(background)
                .tag(Result.DIRECT.name)
                .build())

        dialog = MaterialDialog.Builder(context)
                .adapter(adapter, null)
                .negativeText(R.string.cancel)
                .dismissListener { processor.onComplete() }
                .autoDismiss(true)
                .build()
    }

    fun show(): Flowable<Result> {
        dialog.show()
        return processor
    }
}
