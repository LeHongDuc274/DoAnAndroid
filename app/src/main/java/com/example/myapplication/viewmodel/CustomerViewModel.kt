package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.api.OrderApi
import com.example.myapplication.core.api.ProductApi
import com.example.myapplication.core.api.response.CategoriesListResponse
import com.example.myapplication.core.api.response.OrderDetailRes
import com.example.myapplication.core.api.response.OrderResponse
import com.example.myapplication.core.api.response.products.ProductList
import com.example.myapplication.core.model.CategoryEntity
import com.example.myapplication.core.model.Order
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.core.utils.Utils
import com.example.myapplication.ext.AccessToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerViewModel(private val app: Application) : AndroidViewModel(app) {
    private var token = ""
    val listCategories = MutableStateFlow<MutableList<CategoryEntity>>(mutableListOf())
    val listProducts = MutableStateFlow<MutableList<ProductEntity>>(mutableListOf())
    val listOrderDetails = MutableStateFlow<List<OrderDetail>>(listOf())
    val totalAmount = MutableStateFlow<Int>(0)
    val listProductFilter =
        MutableStateFlow<MutableList<ProductEntity>>(mutableListOf())
    var categorySelected = -1
    var order: Order = Order()

    init {
        token = app.AccessToken()
        val productApi = ProductApi.createProductApi(token)
        viewModelScope.launch {
            listProducts.collect {
                Utils.setListProduct(it)
            }
        }
        productApi.getListCategories().enqueue(object : Callback<CategoriesListResponse> {
            override fun onResponse(
                call: Call<CategoriesListResponse>,
                response: Response<CategoriesListResponse>
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

            override fun onFailure(call: Call<CategoriesListResponse>, t: Throwable) {
                Log.e("tag", t.message.toString())
            }

        })

        productApi.getListProduct().enqueue(object : Callback<ProductList> {
            override fun onResponse(call: Call<ProductList>, response: Response<ProductList>) {
                if (response.isSuccessful) {
                    val listdata = response.body()!!.data.toMutableList()
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


    fun setListOrder(list: MutableList<OrderDetail>) {
        viewModelScope.launch {
            listOrderDetails.emit(list)
        }
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

    fun createOrder(onDone: (Boolean, String, Order?) -> Unit) {
        val api = OrderApi.createOrderApi(token)
        val res = api.createOrder(order)
        res.enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()!!.data
                    order = data
                    listOrderDetails.value = data.order_details
                    onDone.invoke(true, "Succes", data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }

        })
    }

    fun createOrderDetail(detail: OrderDetail, onDone: (Boolean, String, OrderDetail?) -> Unit) {
        val api = OrderApi.createOrderApi(token)
        val res = api.createOrderDetails(detail)
        res.enqueue(object : Callback<OrderDetailRes> {
            override fun onResponse(call: Call<OrderDetailRes>, response: Response<OrderDetailRes>) {
                if (response.isSuccessful) {
                    onDone.invoke(true, "Create detail succes", response.body()!!.data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<OrderDetailRes>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }
        })


    }

    fun updateOrderDetails(detail: OrderDetail, onDone: (Boolean, String, OrderDetail?) -> Unit) {
        val api = OrderApi.createOrderApi(token)
        val res = api.updateOrderDetails(detail)
        res.enqueue(object : Callback<OrderDetailRes> {
            override fun onResponse(call: Call<OrderDetailRes>, response: Response<OrderDetailRes>) {
                if (response.isSuccessful) {
                    onDone.invoke(true, "Update Order detail succes", response.body()!!.data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<OrderDetailRes>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }
        })

    }

    private fun sum(pr: ProductEntity): Int {
        return ((pr.countOrder) * (pr.price))
    }

}

