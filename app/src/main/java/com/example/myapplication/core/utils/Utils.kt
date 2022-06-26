package com.example.myapplication.core.utils

import android.content.Context
import android.widget.Toast
import com.example.myapplication.core.ItemStatus
import com.example.myapplication.core.model.ProductEntity

object Utils {
    var listProducts = mutableListOf<ProductEntity>()

    fun setListProduct(list: MutableList<ProductEntity>) {
        listProducts.clear()
        listProducts.addAll(list)
    }

    fun getProduct(product_id: Int): ProductEntity? {
        return listProducts.findLast {
            it.id == product_id
        }
    }

    fun getByStatus(i : Int) : ItemStatus?{
        return ItemStatus.values().find {
            it.status == i
        }
    }
}

