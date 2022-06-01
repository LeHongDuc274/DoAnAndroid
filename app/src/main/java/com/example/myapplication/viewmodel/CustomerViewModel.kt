package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.core.model.CategoriesEntity
import com.example.myapplication.core.model.ProductEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CustomerViewModel(private val app: Application) : AndroidViewModel(app) {

    val listCategories = MutableStateFlow<MutableList<CategoriesEntity>>(mutableListOf())
    val listProducts = MutableStateFlow<MutableList<ProductEntity>>(mutableListOf())

    val listOrder = MutableLiveData<MutableList<ProductEntity>>().apply { value = mutableListOf() }

    val totalAmount = MutableStateFlow<Int>(0)

    init {
        val list = mutableListOf<ProductEntity>()
        val listCate = mutableListOf<CategoriesEntity>()
        repeat(15) {
            list.add(
                ProductEntity(
                    name = "name ${it + 1}",
                    id = it + 1,
                    price = (it + 1).times(10000),
                    disciption = "discription ${it + 1}",
                    status = it % 2
                )
            )
            listCate.add(
                CategoriesEntity(id = it + 1, name = " cate $it")
            )
        }
        listProducts.value = list
        listCategories.value = listCate
    }

    fun setListOrder() {
        listOrder.value = listProducts.value.filter {
            it.countOrder > 0
        }.toMutableList()
        totalAmount.value = listOrder.value?.toList()?.sumOf {
            sum(it)
        } ?: 0
    }

    private fun sum(pr: ProductEntity): Int {
        return ((pr.countOrder) * (pr.price))
    }
}

