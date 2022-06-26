package com.example.myapplication.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.utils.Utils
import com.example.myapplication.databinding.OrderDetailItemKitchenBinding

class OrderDetailKitchenAdapter : RecyclerView.Adapter<OrderDetailKitchenAdapter.ViewHolder>() {
    private var listOrder = mutableListOf<OrderDetail>()
    private var onBtnClick: ((OrderDetail) -> Unit)? = null

    inner class ViewHolder(val binding: OrderDetailItemKitchenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val orderDetail = listOrder[layoutPosition]
            val product = Utils.getProduct(orderDetail.product_id)
            binding.tvAmount.text = orderDetail.amount.toString()
            binding.tvNote.text = orderDetail.note
            product?.let {
                binding.tvName.text = it.name
            }
            binding.tvUserName.text = orderDetail.user_display_name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            OrderDetailItemKitchenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        initListener(binding, viewHolder)
        return viewHolder
    }

    private fun initListener(binding: OrderDetailItemKitchenBinding, holder: ViewHolder) {
        binding.ivNext.setOnClickListener {
            onBtnClick?.invoke(listOrder[holder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount() = listOrder.size


    fun setOnClick(action: (OrderDetail) -> Unit) {
        onBtnClick = action
    }

    fun setData(list: MutableList<OrderDetail>) {
        listOrder = list
        notifyDataSetChanged()
    }
}