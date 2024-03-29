package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.core.ItemStatus
import com.example.myapplication.ui.model.OrderDetail
import com.example.myapplication.utils.Utils
import com.example.myapplication.databinding.OrderItemLineBinding
import com.example.myapplication.ext.formatWithCurrency

class OrderAdapter(val type: Int = TYPE_CUSTOMER) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
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
                binding.tvPrice.text = ((it.price) * (orderDetail.amount)).formatWithCurrency()
                Glide.with(binding.root.context).load(it.image_url).into(binding.ivProduct)
            }
            binding.tvCount.text = orderDetail.amount.toString()
            val status = Utils.getByStatus(orderDetail.status)
            status?.let {
                binding.tvStatus.text = it.title
                if (it.status > ItemStatus.PENDING.status && type == TYPE_CUSTOMER){
                    binding.cvDelete.visibility = View.INVISIBLE
                } else {
                    binding.cvDelete.visibility = View.VISIBLE
                }
            }
            if (orderDetail.note.isBlank()) {
                binding.tvNote.text = "Ghi chú ..."
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
    companion object{
        const val TYPE_ADMIN = 1
        const val TYPE_CUSTOMER = 0
    }
}