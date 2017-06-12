package jp.gr.java_conf.miwax.troutoss.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.graphics.Bitmap
import com.android.databinding.library.baseAdapters.BR
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.extension.AttachmentType
import jp.gr.java_conf.miwax.troutoss.extension.getImageThumbnail
import jp.gr.java_conf.miwax.troutoss.extension.getVideoThumbnail
import jp.gr.java_conf.miwax.troutoss.model.AttachmentHolder
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

/**
 * Created by Tomoya Miwa on 2017/06/12.
 * AttachmentThumbnail用のViewModel
 */

class AttachmentThumbnailViewModel(private val attachment: AttachmentHolder.Attachment) :
        BaseObservable() {

    @get:Bindable
    var thumbnail: Bitmap? = null

    @get:Bindable
    var progress = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.progress)
        }

    @get:Bindable
    val resultImage: Int?
        get() = result?.let { if (it) R.drawable.check else R.drawable.error }

    var result: Boolean? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.resultImage)
        }

    init {
        launch(CommonPool) {
            thumbnail = when (attachment.type) {
                AttachmentType.IMAGE -> attachment.uri.getImageThumbnail()
                AttachmentType.VIDEO -> attachment.uri.getVideoThumbnail()
                else -> async(CommonPool) { null }
            }.await()
            launch(UI) { notifyPropertyChanged(BR.thumbnail) }
        }
    }
}
