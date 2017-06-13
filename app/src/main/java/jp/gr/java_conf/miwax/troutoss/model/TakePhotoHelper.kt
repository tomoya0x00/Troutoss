package jp.gr.java_conf.miwax.troutoss.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import jp.gr.java_conf.miwax.troutoss.App.Companion.appContext
import jp.gr.java_conf.miwax.troutoss.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Tomoya Miwa on 2017/06/13.
 * カメラでの写真撮影用ヘルパー
 */

object TakePhotoHelper {

    private val INTENT_BUNDLE = "intent_bundle"
    private val BUNDLE_PHOTO_PATH = "bundle_photo_path"

    @Throws(IOException::class)
    fun createImageFile(context: Context): Uri {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp
        val storageDir = appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        val photoUri = FileProvider.getUriForFile(
                context,
                "${context.getString(R.string.applicationName)}.fileprovider",
                image)

        return photoUri
    }

    fun getTakePictureIntent(uri: Uri, context: Context): Intent? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context.packageManager) != null) {
            return intent.apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
        }

        return null
    }

    fun addPhotoToGallery(uri: Uri) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = uri
        appContext.sendBroadcast(intent)
    }
}
