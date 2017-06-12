package jp.gr.java_conf.miwax.troutoss.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.webkit.MimeTypeMap
import jp.gr.java_conf.miwax.troutoss.App.Companion.appContext
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.threeten.bp.LocalDate.now
import java.io.IOException
import java.util.*


/**
 * Created by Tomoya Miwa on 2017/06/11.
 * Uri用のExtension
 */

@Throws(IOException::class)
fun Uri.getBitmap(): Deferred<Bitmap?> = async(CommonPool) {
    appContext.contentResolver.openFileDescriptor(this@getBitmap, "r").use { parcelFileDescriptor ->
        val fileDescriptor = parcelFileDescriptor.fileDescriptor
        return@async BitmapFactory.decodeFileDescriptor(fileDescriptor)
    }
}

fun Uri.readBytes(): Deferred<ByteArray> = async(CommonPool) {
    appContext.contentResolver.openInputStream(this@readBytes).use {
        return@async it.readBytes()
    }
}

@Throws(IOException::class)
fun Uri.getImageThumbnail(): Deferred<Bitmap?> = async(CommonPool) {
    val src = this@getImageThumbnail.getBitmap()
    src.await()?.let {
        val thumbnail = ThumbnailUtils.extractThumbnail(it, 512, 384, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
        it.recycle()
        return@async thumbnail
    }

    return@async null
}

fun Uri.getMimeType(): String {
    return appContext.contentResolver.getType(this) ?:
            MimeTypeMap.getFileExtensionFromUrl(this.toString())?.let {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
            } ?: "unknown/unknown"
}

fun Uri.generateFileName(): String {
    val randomNo = Random().nextInt(1000)
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getMimeType())

    return buildString {
        append("Troutoss_${now()}_$randomNo")
        extension?.run { append(".$extension") }
    }
}

