package jp.gr.java_conf.miwax.troutoss.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import jp.gr.java_conf.miwax.troutoss.App.Companion.appContext
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.IOException


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
