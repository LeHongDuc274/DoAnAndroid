package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.core.api.response.TableOrdering
import com.example.myapplication.databinding.TableOrderItemBinding

class TableOrderAdapter : RecyclerView.Adapter<TableOrderAdapter.ViewHolder>() {
    private var listTable = mutableListOf<TableOrdering>()
    private var callback: ((TableOrdering) -> Unit)? = null

    inner class ViewHolder(val binding: TableOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            binding.tvDisplayName.text = listTable[layoutPosition].display_name
            val order = listTable[layoutPosition].order
            if (order == null || order.status == 1 ){
                binding.tvState.text = "Empty"
            } else {
                binding.tvState.text = "Using"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            TableOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewholder = ViewHolder(binding)
        initListener(viewholder)
        return viewholder
    }

    private fun initListener(viewholder: TableOrderAdapter.ViewHolder) {
        viewholder.itemView.setOnClickListener {
            callback?.invoke(listTable[viewholder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount() = listTable.size

    fun setData(list: MutableList<TableOrdering>) {
        listTable.clear()
        listTable.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnItemClick(listener: (TableOrdering)->Unit){
        callback = listener
    }
}