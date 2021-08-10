package com.main.notificationapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.main.notificationapp.databinding.FirstItemLayoutBinding
import com.main.notificationapp.databinding.ItemLayoutBinding
import com.main.notificationapp.models.Article
import java.util.*

class NewsAdapter(var isSavedFragment: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

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
        }else{
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
        }
    }

    override fun getItemCount(): Int {
        return diffList.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && !isSavedFragment) 1
        else 2
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
}

interface NewsItemClickListener{
    fun onItemClick(article: Article)
}