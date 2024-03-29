package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ui.model.ProductEntity
import com.example.myapplication.databinding.ProductManagerItemBinding
import com.example.myapplication.ext.formatWithCurrency

class ProductManagerAdapter : RecyclerView.Adapter<ProductManagerAdapter.ViewHolder>() {
    private var listProduct = mutableListOf<ProductEntity>()
    private var onItemClick : ((ProductEntity) -> Unit)? = null
    inner class ViewHolder(val binding: ProductManagerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (listProduct[layoutPosition].id == -1) {
                binding.tvName.visibility = View.INVISIBLE
                binding.tvPrice.visibility = View.INVISIBLE
                binding.ivProduct.visibility = View.INVISIBLE
                binding.tvNew.visibility = View.INVISIBLE
                binding.tvStatus.visibility = View.INVISIBLE
                binding.tvEdit.visibility = View.INVISIBLE
                binding.tvNew.visibility = View.VISIBLE
            } else {
                binding.tvName.visibility = View.VISIBLE
                binding.tvPrice.visibility = View.VISIBLE
                binding.ivProduct.visibility = View.VISIBLE
                binding.tvNew.visibility = View.VISIBLE
                binding.tvStatus.visibility = View.VISIBLE
                binding.tvEdit.visibility = View.VISIBLE
                binding.tvNew.visibility = View.INVISIBLE
                val product = listProduct[layoutPosition]
                binding.tvName.text = product.name
                binding.tvPrice.text = product.price.formatWithCurrency()
                binding.tvStatus.text = if (product.status == 0 ) "Khả dụng" else "Hết hàng"
                val url = listProduct[layoutPosition].image_url
                Glide.with(binding.ivProduct.context).load(url).circleCrop().into(binding.ivProduct)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ProductManagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        initListener(binding, holder)
        return holder
    }

    private fun initListener(binding: ProductManagerItemBinding, holder: ViewHolder) {
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(listProduct[holder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = listProduct.size

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_NEW else TYE_NORMAL

    fun setData(list: List<ProductEntity>) {
        listProduct = list.toMutableList()
        listProduct.add(0, ProductEntity(-1, "", "", 0, 1,-1,null,"",""))
        notifyDataSetChanged()
    }

    fun setRawData(list: List<ProductEntity>) {
        listProduct = list.toMutableList()
        notifyDataSetChanged()
    }

    fun onClick(action: (ProductEntity)->Unit){
        onItemClick = action
    }

    companion object {
        const val TYE_NORMAL = 0
        const val TYPE_NEW = 1
    }
}