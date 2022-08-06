package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.TableHistoryLayoutBinding
import com.example.myapplication.ext.formatWithCurrency
import com.example.myapplication.ui.model.Order

class TableHistoryAdapter : RecyclerView.Adapter<TableHistoryAdapter.ViewHolder>() {
    private var listOrder = mutableListOf<Order>()
    private var listTable = mutableListOf<String>()
    private var mapTable = mapOf<String, List<Order>>()
    private var onItemClick: ((String,List<Order>) -> Unit)? = null

    inner class ViewHolder(val binding: TableHistoryLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val name = listTable[layoutPosition]
            binding.tvDisplayName.text = name
            val countOrder = mapTable[name]?.size ?: 0
            binding.tvCountOrders.text = "Số đơn hàng : $countOrder"
            val revenue = mapTable[name]?.sumOf {
                it.total_price
            } ?: 0
            binding.tvRevenue.text = "Doanh thu : ${revenue.formatWithCurrency()}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            TableHistoryLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val viewholder = ViewHolder(binding)
        viewholder.binding.root.setOnClickListener {
            val name = listTable[viewholder.layoutPosition]
            val list = mapTable[name] ?: listOf<Order>()
            onItemClick?.invoke(name,list)
        }
        return viewholder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int = listTable.size

    fun setData(list: List<Order>) {
        listOrder.clear()
        listOrder.addAll(list)
        listTable.clear()
        mapTable = mapOf<String,MutableList<Order>>().plus(Pair("Tất cả", listOrder)).plus(listOrder.groupBy { it.user_name })
        listTable.addAll(mapTable.keys.toMutableList())
        notifyDataSetChanged()
    }

    fun setOnItemClick(callback: (String,List<Order>) -> Unit) {
        onItemClick = callback
    }

}