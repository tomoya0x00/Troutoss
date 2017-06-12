package jp.gr.java_conf.miwax.troutoss.extension

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
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

fun Uri.getVideoThumbnail(): Deferred<Bitmap?> = async(CommonPool) {
    this@getVideoThumbnail.getRealPath()?.let {
        val thumbnail = ThumbnailUtils.createVideoThumbnail(it, MediaStore.Video.Thumbnails.MINI_KIND)
        return@async thumbnail
    }

    return@async null
}

fun Uri.getRealPath(): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(appContext, this)) {
        if ("com.android.externalstorage.documents" == this.authority) {// ExternalStorageProvider
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return "${Environment.getExternalStorageDirectory()}/${split[1]}"
            } else {
                return "/stroage/$type/${split[1]}"
            }
        } else if ("com.android.providers.downloads.documents" == this.authority) {// DownloadsProvider
            val id = DocumentsContract.getDocumentId(this)
            val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)
            return getDataColumn(appContext, contentUri, null, null)
        } else if ("com.android.providers.media.documents" == this.authority) {// MediaProvider
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            contentUri = MediaStore.Files.getContentUri("external")
            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            return getDataColumn(appContext, contentUri, selection, selectionArgs)
        }
    } else if ("content".equals(this.scheme, ignoreCase = true)) {//MediaStore
        return getDataColumn(appContext, this, null, null)
    } else if ("file".equals(this.scheme, ignoreCase = true)) {// File
        return this.path
    }
    return null
}

fun getDataColumn(context: Context, uri: Uri, selection: String?,
                  selectionArgs: Array<String>?): String? {
    val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
    context.contentResolver.query(
            uri, projection, selection, selectionArgs, null).use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(projection[0])
            return cursor.getString(index)
        }
    }
    return null
}

