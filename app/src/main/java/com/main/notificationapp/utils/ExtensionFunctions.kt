package com.main.notificationapp.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.main.notificationapp.R
import com.main.notificationapp.models.Article


@BindingAdapter("image")
fun setArticleImage(imageView: ImageView, article: Article){
    Glide.with(imageView.context)
        .load(article.urlToImage)
        .placeholder(R.drawable.loading_image)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .error(R.drawable.loading_image)
        .into(imageView)
}

fun EditText.search(action: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(text: Editable?) {
            action(text.toString())
        }
    })
}
