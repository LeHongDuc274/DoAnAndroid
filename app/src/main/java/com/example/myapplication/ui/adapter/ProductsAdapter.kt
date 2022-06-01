package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.databinding.ProductItemBinding

class ProductsAdapter() : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {
    private var callback: ((ProductEntity) -> Unit)? = null

    private var listProduct = mutableListOf<ProductEntity>()

    inner class ViewHolder(val binding: ProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            binding.tvName.text = listProduct[layoutPosition].name
            binding.tvPrice.text = listProduct[layoutPosition].price.toString() + " vnđ"

            if (listProduct[layoutPosition].status == 0) {
                binding.ivStatus.visibility = View.VISIBLE
            } else {
                binding.ivStatus.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        initListener(holder)
        return holder
    }

    private fun initListener(holder: ProductsAdapter.ViewHolder) {
        holder.itemView.setOnClickListener {
            callback?.invoke(listProduct[holder.layoutPosition])
        }
    }

    fun setOnClick(c: (ProductEntity) -> Unit) {
        callback = c
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = listProduct.size

    fun setListData(list: List<ProductEntity>) {
        listProduct = list.toMutableList()
        notifyDataSetChanged()
    }
}