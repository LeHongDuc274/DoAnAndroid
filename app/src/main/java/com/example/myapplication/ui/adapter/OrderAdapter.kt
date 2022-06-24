package com.example.myapplication.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.core.ItemStatus
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.utils.Utils
import com.example.myapplication.databinding.OrderItemLineBinding

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
    private var listOrder = mutableListOf<OrderDetail>()
    private var onDelete: ((OrderDetail) -> Unit)? = null
    private var onEdit: ((OrderDetail) -> Unit)? = null

    inner class ViewHolder(val binding: OrderItemLineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val orderDetail = listOrder[layoutPosition]
            val product = Utils.getProduct(orderDetail.product_id)
            product?.let {
                binding.tvPerPrice.text = it.price.toString()
                binding.tvProduct.text = it.name
                binding.tvPrice.text = ((it.price) * (orderDetail.amount)).toString() + "vnđ"
                Glide.with(binding.root.context).load(it.image_url).into(binding.ivProduct)
                binding.ivProduct
            }
            binding.tvCount.text = orderDetail.amount.toString()
            val status = Utils.getByStatus(orderDetail.status)
            status?.let {
                binding.tvStatus.text = it.title
                if (it.status > ItemStatus.PENDING.status){
                    binding.cvDelete.visibility = View.INVISIBLE
                } else {
                    binding.cvDelete.visibility = View.VISIBLE
                }
            }
            if (orderDetail.note.isBlank()) {
                binding.tvNote.text = "Note..."
            } else {
                binding.tvNote.text = orderDetail.note
            }

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

    fun setDeleteClick(action: (OrderDetail) -> Unit) {
        onDelete = action
    }

    fun setOnEditClick(action: (OrderDetail) -> Unit) {
        onEdit = action
    }

    fun setData(list: MutableList<OrderDetail>) {
        listOrder = list
        notifyDataSetChanged()
    }
}