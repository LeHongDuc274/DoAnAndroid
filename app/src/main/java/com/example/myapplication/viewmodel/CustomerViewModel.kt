package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.api.ProductApi
import com.example.myapplication.core.api.response.CategoriesList
import com.example.myapplication.core.api.response.products.ProductList
import com.example.myapplication.core.model.CategoriesEntity
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.ext.AccessToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerViewModel(private val app: Application) : AndroidViewModel(app) {
    private var token = ""
    val listCategories = MutableStateFlow<MutableList<CategoriesEntity>>(mutableListOf())
    val listProducts = MutableStateFlow<MutableList<ProductEntity>>(mutableListOf())
    val listOrder = MutableLiveData<MutableList<ProductEntity>>().apply { value = mutableListOf() }
    val totalAmount = MutableStateFlow<Int>(0)
    val listProductFilter =
        MutableStateFlow<MutableList<ProductEntity>>(mutableListOf())
    var categorySelected = -1

    init {
        token = app.AccessToken()
        val productApi = ProductApi.createProductApi(token)

        productApi.getListCategories().enqueue(object : Callback<CategoriesList> {
            override fun onResponse(
                call: Call<CategoriesList>,
                response: Response<CategoriesList>
            ) {
                if (response.isSuccessful) {
                    val listdata = response.body()!!.data.toMutableList()
                    viewModelScope.launch {
                        listCategories.emit(listdata)
                    }
                } else {
                    Log.e("tag", response.code().toString())
                }
            }

            override fun onFailure(call: Call<CategoriesList>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

        productApi.getListProduct().enqueue(object : Callback<ProductList> {
            override fun onResponse(call: Call<ProductList>, response: Response<ProductList>) {
                if (response.isSuccessful) {
                    val listdata = response.body()!!.data.toMutableList()
                    Log.e("tagPrice",listdata.toString())

                    viewModelScope.launch {
                        listProducts.emit(listdata)
                        setListProductByCategory(categorySelected)
                    }
                } else {
                    Log.e("tag", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ProductList>, t: Throwable) {
                Log.e("tag", t.message.toString())
            }
        })
    }

    fun setListOrder() {
        listOrder.value = listProducts.value.filter {
            it.countOrder > 0
        }.toMutableList()
        totalAmount.value = listOrder.value?.toList()?.sumOf {
            sum(it)
        } ?: 0
    }

    fun setListProductByCategory(id: Int = categorySelected) {
        categorySelected = id
        if (categorySelected == -1) {
            viewModelScope.launch {
                listProductFilter.emit(listProducts.value)
            }
        } else {
            val listData = listProducts.value.filter {
                it.category_id == id
            }
            viewModelScope.launch {
                listProductFilter.emit(listData.toMutableList())
            }
        }
    }

    private fun sum(pr: ProductEntity): Int {
        return ((pr.countOrder) * (pr.price))
    }
}

