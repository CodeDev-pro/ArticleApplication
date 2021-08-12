package com.main.notificationapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.main.notificationapp.databinding.FirstItemLayoutBinding
import com.main.notificationapp.databinding.ItemLayoutBinding
import com.main.notificationapp.databinding.LayoutLastItemBinding
import com.main.notificationapp.databinding.LayoutSourcesItemBinding
import com.main.notificationapp.models.Article
import com.main.notificationapp.ui.viewmodels.MainViewModel
import com.main.notificationapp.utils.NewsCacheOperations
import java.util.*

class NewsAdapter(
    var isSavedFragment: Boolean = false,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val viewModel: MainViewModel? = null
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var itemClickListener: NewsItemClickListener? = null
    private val itemCallback = object: DiffUtil.ItemCallback<Article>(){
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.urlToImage == newItem.urlToImage
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

    }

    val diffList = AsyncListDiffer(this, itemCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1){
            FirstItemViewHolder(
                FirstItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }else if(viewType == 2){
            LastItemViewHolder(
                LayoutLastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            ItemViewHolder(
                ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val article = diffList.currentList[position]?.apply {
            publishedAt.replace("T", " ")
                .replace("Z", " ")
        }

        if(!isSavedFragment) {
            viewModel?.topHeadlinesLastValue?.observe(lifecycleOwner!!) {
                when(it) {
                    is NewsCacheOperations.Loading -> {
                        (holder as LastItemViewHolder).show()
                    }
                    is NewsCacheOperations.Error -> {
                        (holder as LastItemViewHolder).hide()
                        Snackbar.make(holder.itemView, "An Unknown Error Occurred, Please try again", Snackbar.LENGTH_LONG).apply {
                            setAction("Retry") {
                                viewModel.getMoreArticles()
                            }
                        }.show()
                    }
                    is NewsCacheOperations.Success -> {
                        (holder as LastItemViewHolder).hide()
                    }
                }
            }
        }

        val random = Random().nextInt(10)
        when(holder){
            is ItemViewHolder ->{
                if (article!=null) {
                    holder.bind(article)
                    holder.itemView.setOnClickListener {
                        itemClickListener?.onItemClick(article)
                    }
                }

            }
            is FirstItemViewHolder->{
                val viewHolder = holder
                val firstArticle = diffList.currentList[random]?.apply {
                    publishedAt.replace("T", " ")
                    publishedAt.replace("Z", " ")
                }
                if (firstArticle!=null){
                    viewHolder.bind(firstArticle)
                    holder.itemView.setOnClickListener{
                        itemClickListener?.onItemClick(firstArticle)
                    }
                }
            }
            is LastItemViewHolder -> {

            }
        }
    }

    override fun getItemCount(): Int {
        return diffList.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && !isSavedFragment) 1
        else if(position == diffList.currentList.size) 2
        else 3
    }

    inner class ItemViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(article: Article){
            binding.article = article
            binding.executePendingBindings()
        }
    }

    inner class FirstItemViewHolder(private val binding: FirstItemLayoutBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(article: Article){
            binding.article = article
            binding.executePendingBindings()
        }
    }

    inner class LastItemViewHolder(private val binding: LayoutLastItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun show(){
            binding.progressBar.visibility = View.VISIBLE
        }

        fun hide() {
            binding.progressBar.visibility = View.GONE
        }
    }
}

interface NewsItemClickListener{
    fun onItemClick(article: Article)
}