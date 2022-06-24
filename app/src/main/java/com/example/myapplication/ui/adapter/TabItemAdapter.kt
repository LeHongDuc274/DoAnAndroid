package com.example.myapplication.ui.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.core.TabItem
import com.example.myapplication.databinding.TabItemBinding
import com.example.myapplication.ext.getDrawableByName

class TabItemAdapter(private val context: Context) :
    RecyclerView.Adapter<TabItemAdapter.ViewHolder>() {
    private var selectedPos = 0
    private var listTab = listOf<TabItem>()
    private var onClick: ((Int) -> Unit)? = null

    inner class ViewHolder(val binding: TabItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(holder: ViewHolder, position: Int) {
            binding.ivTab.setImageResource(context.getDrawableByName(listTab[position].icon))
            holder.itemView.isSelected = selectedPos == position

            if (holder.itemView.isSelected) {
                binding.ivTab.setColorFilter(
                    Color.parseColor("#FFFFFF"),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.ivTab.setColorFilter(
                    Color.parseColor("#D38B33"),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TabItemBinding.inflate(LayoutInflater.from(parent.context))
        val holder = ViewHolder(binding)
        initListener(holder)
        return holder
    }

    private fun initListener(holder: ViewHolder) {
        holder.itemView.setOnClickListener {
            val tempPos = selectedPos
            selectedPos = holder.layoutPosition
            notifyItemChanged(tempPos)
            notifyItemChanged(selectedPos)
            onClick?.invoke(holder.layoutPosition)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(holder, position)
    }

    override fun getItemCount() = listTab.size

    fun setData(list: MutableList<TabItem>) {
        listTab = list
        notifyDataSetChanged()
    }

    fun setOnClick(action: (Int) -> Unit) {
        onClick = action
    }

    fun setSelectedItem(pos : Int ){
        selectedPos = pos
        notifyDataSetChanged()
    }
}