package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ui.model.CategoryEntity
import com.example.myapplication.ui.model.ProductEntity
import com.example.myapplication.databinding.CategoryItemBinding
import com.example.myapplication.databinding.ProductItemBinding
import com.example.myapplication.ext.formatWithCurrency

class ProductsAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var callback: ((ProductEntity) -> Unit)? = null
    private var listCategory = mutableListOf<CategoryEntity>()
    private var listProduct = mutableListOf<ProductEntity>()

    inner class ViewHolder(val binding: ProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            binding.tvName.text = listProduct[layoutPosition].name
            binding.tvPrice.text = listProduct[layoutPosition].price.formatWithCurrency()

            if (listProduct[layoutPosition].status != 0) {
                binding.ivStatus.visibility = View.VISIBLE
            } else {
                binding.ivStatus.visibility = View.GONE
            }
            val url = listProduct[layoutPosition].image_url
            Glide.with(binding.ivProduct.context).load(url).circleCrop().into(binding.ivProduct)
        }
    }

    inner class CategoryViewHolder(val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            binding.tvCategory.text = listProduct[layoutPosition].category_name ?: ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_CATEGORY) {
            val binding =
                CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val holder = CategoryViewHolder(binding)
            return holder
        } else {
            val binding =
                ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val holder = ViewHolder(binding)
            initListener(holder)
            return holder
        }
    }

    private fun initListener(holder: ProductsAdapter.ViewHolder) {
        holder.itemView.setOnClickListener {
            callback?.invoke(listProduct[holder.layoutPosition])
        }
    }

    fun setOnClick(c: (ProductEntity) -> Unit) {
        callback = c
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_CATEGORY) (holder as CategoryViewHolder).onBind(position) else (holder as ViewHolder).onBind(
            position
        )
    }

    override fun getItemCount() = listProduct.size

    override fun getItemViewType(position: Int): Int =
        if (listProduct[position].id == -1) TYPE_CATEGORY else TYPE_ITEM

    fun setCategory(list: List<CategoryEntity>) {
        listCategory = list.toMutableList()
        setListData(listProduct)
        notifyDataSetChanged()
    }

    fun setListData(list: List<ProductEntity>) {
        listProduct.clear()
        list.groupBy { product ->
            product.category_id
        }.forEach { map ->
            val category = listCategory.firstOrNull { it.id == map.key }
            category?.let { cate ->
                listProduct.add(ProductEntity().apply {
                    category_name = cate.name_type
                })
            }
            listProduct.addAll(map.value)
        }
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_CATEGORY = 1
    }
}