package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.annotation.StringRes
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ContentImageBinding
import java.lang.Exception

/**
 * Created by Tomoya Miwa on 2017/05/12.
 * 画像用のViewPageAdapter
 */

class ImagesPageAdapter(private val context: Context, private val urls: Array<String>) :
        PagerAdapter() {

    private var toast: Toast? = null

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding: ContentImageBinding = DataBindingUtil.bind(LayoutInflater.from(context).inflate(R.layout.content_image, container, false))

        Glide.with(context).load(urls[position])
                .listener(object : RequestListener<String, GlideDrawable> {
                    override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        binding.progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                        binding.progressBar.visibility = View.GONE
                        showToast(R.string.error_load_image, Toast.LENGTH_SHORT)
                        return false
                    }
                })
                .into(binding.image)
        container.addView(binding.root)

        return binding
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any?) {
        val binding = any as ContentImageBinding
        container.removeView(binding.root)
    }

    override fun getCount(): Int {
        return urls.size
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        val binding = any as ContentImageBinding
        return view == binding.root
    }

    private fun showToast(@StringRes resId: Int, duration: Int) {
        toast?.cancel()
        toast = Toast.makeText(context, resId, duration)
        toast?.show()
    }
}
