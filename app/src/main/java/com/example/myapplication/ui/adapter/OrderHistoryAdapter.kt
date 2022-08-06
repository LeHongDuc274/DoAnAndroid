package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.OrderHistoryItemBinding
import com.example.myapplication.ext.formatWithCurrency
import com.example.myapplication.ui.model.Order

class OrderHistoryAdapter : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {
    private val listOrders = mutableListOf<Order>()
    private var onViewDetail: ((Order) -> Unit)? = null

    inner class ViewHolder(val binding: OrderHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            listOrders[layoutPosition].let {
                binding.tvTime.text = it.created_at
                binding.tvUserName.text = it.user_name
                binding.tvTotalPrice.text = "Tổng tiền :" + it.total_price.formatWithCurrency()
                binding.tvId.text = "ID: ${it.id}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            OrderHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        initListener(holder)
        return holder
    }

    private fun initListener(holder: OrderHistoryAdapter.ViewHolder) {
        holder.binding.tvDetail.setOnClickListener {
            onViewDetail?.invoke(listOrders[holder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int = listOrders.size

    fun setData(list: MutableList<Order>) {
        listOrders.clear()
        listOrders.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnViewDetail(callback: (Order) -> Unit) {
        onViewDetail = callback
    }
}