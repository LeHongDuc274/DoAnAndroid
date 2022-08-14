package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.model.CategoryEntity
import com.example.myapplication.databinding.CategoryItemBinding

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    private var listCategories = mutableListOf<CategoryEntity>()
    private var onEdit: ((CategoryEntity) -> Unit)? = null
    private var onDelete: ((CategoryEntity) -> Unit)? = null

    inner class ViewHolder(val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val category = listCategories[layoutPosition]
            binding.tvCategory.text = category.name_type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        initListener(binding, viewHolder)
        return viewHolder
    }

    private fun initListener(binding: CategoryItemBinding, holder: ViewHolder) {
        binding.tvEdit.setOnClickListener {
            onEdit?.invoke(listCategories[holder.layoutPosition])
        }
        binding.tvDelete.setOnClickListener {
            onDelete?.invoke(listCategories[holder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount() = listCategories.size

    fun setOnEditClick(action: (CategoryEntity) -> Unit) {
        onEdit = action
    }

    fun setOnDeleteClick(action: (CategoryEntity) -> Unit) {
        onDelete = action
    }

    fun setData(list: MutableList<CategoryEntity>) {
        listCategories = list
        notifyDataSetChanged()
    }
}