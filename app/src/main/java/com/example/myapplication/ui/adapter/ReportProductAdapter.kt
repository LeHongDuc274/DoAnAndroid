package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.api.response.ProductReport
import com.example.myapplication.utils.Utils
import com.example.myapplication.databinding.ProductReportItemBinding
import com.example.myapplication.ext.formatWithCurrency

class ReportProductAdapter() : RecyclerView.Adapter<ReportProductAdapter.ViewHolder>() {
    private var listProductReport = mutableListOf<ProductReport>()

    inner class ViewHolder(val binding: ProductReportItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val report = listProductReport[layoutPosition]
            val product = Utils.getProduct(product_id = report.product)
            report.let {
                binding.tvProductName.text = it.product_name
                binding.tvAmount.text = it.count.toString() + " sản phẩm đã bán"
                binding.tvRevenue.text = it.revenue.formatWithCurrency()
                Glide.with(binding.root.context).load(product?.image_url).into(binding.ivProduct)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ProductReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        return viewHolder
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount() = listProductReport.size

    fun setData(list: MutableList<ProductReport>) {
        listProductReport = list
        notifyDataSetChanged()
    }
}