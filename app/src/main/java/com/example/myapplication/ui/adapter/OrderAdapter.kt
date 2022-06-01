package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.databinding.OrderItemLineBinding

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
    private var listOrder = mutableListOf<ProductEntity>()
    private var onDelete: ((ProductEntity) -> Unit)? = null
    private var onEdit: ((ProductEntity) -> Unit)? = null

    inner class ViewHolder(val binding: OrderItemLineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val product = listOrder[layoutPosition]
            binding.tvCount.text = product.countOrder.toString()
            binding.tvPerPrice.text = product.price.toString()
            binding.tvProduct.text = product.name
            if (product.note.isBlank()) {
                binding.tvNote.text = "Note..."
            } else {
                binding.tvNote.text = product.note
            }
            binding.tvPrice.text = ((product.price) * (product.countOrder)).toString() + "vnđ"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            OrderItemLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        initListener(binding, viewHolder)
        return viewHolder
    }

    private fun initListener(binding: OrderItemLineBinding, holder: ViewHolder) {
        binding.cvDelete.setOnClickListener {
            onDelete?.invoke(listOrder[holder.layoutPosition])
        }
        binding.root.setOnClickListener {
            onEdit?.invoke(listOrder[holder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount() = listOrder.size

    fun setDeleteClick(action: (ProductEntity) -> Unit) {
        onDelete = action
    }

    fun setOnEditClick(action: (ProductEntity) -> Unit) {
        onEdit = action
    }

    fun setData(list: MutableList<ProductEntity>) {
        listOrder = list
        notifyDataSetChanged()
    }
}