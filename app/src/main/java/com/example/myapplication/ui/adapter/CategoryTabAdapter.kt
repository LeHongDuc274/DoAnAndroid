package com.example.myapplication.ui.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.core.model.CategoriesEntity
import com.example.myapplication.databinding.CategoryTabItemBinding

class CategoryTabAdapter(private val context: Context) :
    RecyclerView.Adapter<CategoryTabAdapter.ViewHolder>() {
    private var listCategories = mutableListOf<CategoriesEntity>()
    private var selectedPos = 0
    private var onClick: ((CategoriesEntity) -> Unit)? = null

    inner class ViewHolder(val binding: CategoryTabItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvCategory.text = listCategories[layoutPosition].name
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
            Log.e("tagzz",selectedPos.toString())
            onClick?.invoke(listCategories[holder.layoutPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = listCategories.size

    fun setData(list: MutableList<CategoriesEntity>) {
        listCategories = list
        notifyDataSetChanged()
    }

    fun setOnClickItem(action: (CategoriesEntity) -> Unit) {
        onClick = action
    }
}