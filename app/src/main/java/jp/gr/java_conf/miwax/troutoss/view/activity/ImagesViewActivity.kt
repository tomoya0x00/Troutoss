package jp.gr.java_conf.miwax.troutoss.view.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ActivityImagesViewBinding
import jp.gr.java_conf.miwax.troutoss.view.adapter.ImagesPageAdapter


class ImagesViewActivity : AppCompatActivity() {

    lateinit private var binding: ActivityImagesViewBinding

    val imageUrls: Array<String> by lazy { intent.extras.getStringArray(EXTRA_IMAGE_URLS) }
    val showIndex: Int by lazy { intent.extras.getInt(EXTRA_SHOW_INDEX) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityImagesViewBinding>(this, R.layout.activity_images_view)
        binding.imagesPager.adapter = ImagesPageAdapter(this, imageUrls)
        binding.imagesPager.currentItem = showIndex
    }

    companion object {
        private val EXTRA_IMAGE_URLS = "image_urls"
        private val EXTRA_SHOW_INDEX = "show_index"

        fun startActivity(context: Context, urls: Array<String>, index: Int) {
            val intent = Intent(context, ImagesViewActivity::class.java)
            intent.putExtra(EXTRA_IMAGE_URLS, urls)
            intent.putExtra(EXTRA_SHOW_INDEX, index)
            context.startActivity(intent)
        }
    }
}
