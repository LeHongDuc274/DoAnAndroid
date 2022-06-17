package com.example.myapplication.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.core.model.CategoryEntity
import com.example.myapplication.databinding.CategoryTabItemBinding

class CategoryTabAdapter(private val context: Context) :
    RecyclerView.Adapter<CategoryTabAdapter.ViewHolder>() {
    private var listCategories = mutableListOf<CategoryEntity>()
    private var selectedPos = 0
    private var onClick: ((CategoryEntity) -> Unit)? = null

    inner class ViewHolder(val binding: CategoryTabItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvCategory.text = listCategories[layoutPosition].name_type
            if (layoutPosition == selectedPos) {
                binding.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.color_red))
            } else {
                binding.tvCategory.setTextColor(Color.WHITE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            CategoryTabItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        holder.itemView.setOnClickListener {
            val tempPos = selectedPos
            selectedPos = holder.layoutPosition
            notifyItemChanged(tempPos)
            notifyItemChanged(selectedPos)
            onClick?.invoke(listCategories[holder.layoutPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = listCategories.size

    fun setData(list: MutableList<CategoryEntity>) {
        listCategories.clear()
        listCategories.addAll(list)
        listCategories.add(0, CategoryEntity(-1, "All"))
        notifyDataSetChanged()
    }

    fun setOnClickItem(action: (CategoryEntity) -> Unit) {
        onClick = action
    }
}