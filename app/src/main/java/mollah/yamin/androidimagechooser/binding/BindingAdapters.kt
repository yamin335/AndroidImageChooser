package mollah.yamin.androidimagechooser.binding

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener

/**
 * Binding adapters for data binding
 */

class BindingAdapters {
    @BindingAdapter(value = ["imageUrl", "imageRequestListener"], requireAll = false)
    fun bindImage(imageView: ImageView, url: String?, listener: RequestListener<Drawable?>?) {
        Glide.with(imageView.context).load(url).listener(listener).into(imageView)
    }
}



