package com.main.notificationapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.main.notificationapp.databinding.LayoutSourcesItemBinding
import com.main.notificationapp.models.SourceX

class SourcesAdapter : RecyclerView.Adapter<SourcesAdapter.ViewHolder>(){

    var itemClickListener: SourceItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutSourcesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val source = differ.currentList[position]
        holder.bind(source)
        holder.itemView.setOnClickListener{
            itemClickListener?.onItemClick(source)
        }
    }

    inner class ViewHolder(private val binding: LayoutSourcesItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(source: SourceX){
            binding.source = source
            binding.executePendingBindings()
        }
    }

    val diffUtil = object : DiffUtil.ItemCallback<SourceX>(){
        override fun areContentsTheSame(oldItem: SourceX, newItem: SourceX): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: SourceX, newItem: SourceX): Boolean {
            return oldItem.url == newItem.url
        }
    }
    var differ = AsyncListDiffer(this, diffUtil)
}

interface SourceItemClickListener{
    fun onItemClick(source: SourceX)
}