package com.example.myapplication.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.api.OrderService
import com.example.myapplication.api.ProductService
import com.example.myapplication.api.UserService
import com.example.myapplication.api.response.MyCallback
import com.example.myapplication.api.response.MyResult
import com.example.myapplication.api.response.UserResponse
import com.example.myapplication.ui.model.CategoryEntity
import com.example.myapplication.ui.model.Order
import com.example.myapplication.ui.model.OrderDetail
import com.example.myapplication.ui.model.ProductEntity
import com.example.myapplication.utils.Utils
import com.example.myapplication.ext.AccessToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseViewModel(private val app: Application) : AndroidViewModel(app) {
    var token = ""
    val listCategories = MutableStateFlow<MutableList<CategoryEntity>>(mutableListOf())
    val listProducts = MutableStateFlow<MutableList<ProductEntity>>(mutableListOf())
    var connection = false
    var categorySelected = -1
    val listProductFilter =
        MutableStateFlow<MutableList<ProductEntity>>(mutableListOf())

    open fun initViewModel() {
        token = app.AccessToken()
        val productApi = ProductService.createProductApi(token)
        viewModelScope.launch {
            listProducts.collect {
                Utils.setListProduct(it)
            }
        }
        productApi.getListCategories().enqueue(
            MyCallback(onSuccess = {
                viewModelScope.launch {
                    listCategories.emit(it.toMutableList())
                }
            }, {})
        )

        productApi.getListProduct().enqueue(
            MyCallback(onSuccess = {
                viewModelScope.launch {
                    listProducts.emit(it.toMutableList())
                    setListProductByCategory(categorySelected)
                }
            }, {})
        )
    }

    fun updateOrderDetails(detail: OrderDetail, onDone: (Boolean, String, OrderDetail?) -> Unit) {
        val api = OrderService.createOrderApi(token)
        val res = api.updateOrderDetails(detail)
        res.enqueue(
            MyCallback(onSuccess = {
                onDone.invoke(true, "Cập nhập thành công", it)
            }, onError = {
                onDone.invoke(false, it, null)
            })
        )
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

    fun getCurrentOrder(id: Int = -1, onDone: (Boolean, String, MyResult<Order?>?) -> Unit) {
        val api = OrderService.createOrderApi(token)
        val res = api.getCurrentOrder(id)
        res.enqueue(
            object : Callback<MyResult<Order?>> {
                override fun onResponse(
                    call: Call<MyResult<Order?>>,
                    response: Response<MyResult<Order?>>
                ) {
                    if (response.isSuccessful) {
                        onDone.invoke(true, "", response.body())
                    } else {
                        onDone.invoke(false, response.message().toString(), null)
                    }
                }
                override fun onFailure(call: Call<MyResult<Order?>>, t: Throwable) {
                    onDone.invoke(false, t.message.toString(), null)
                }
            }
        )
    }

    fun logout(password: String, onDone: (Boolean, String) -> Unit) {
        val api = UserService.createUserApi(token)
        val paramObject = JSONObject()
        paramObject.put("password", password)
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(paramObject.toString()).toString()
        )
        val res = api.logout(body)
        res.enqueue(
            MyCallback(onSuccess = {
                val sharedPref = app.getSharedPreferences(
                    app.getString(R.string.shared_file_name), Context.MODE_PRIVATE
                ) ?: return@MyCallback
                with(sharedPref.edit()) {
                    putInt(app.getString(com.example.myapplication.R.string.key_role), -1)
                    putString(
                        app.getString(com.example.myapplication.R.string.key_access_token),
                        ""
                    )
                    putString(
                        app.getString(com.example.myapplication.R.string.key_display_name),
                        ""
                    )
                    putInt(app.getString(com.example.myapplication.R.string.key_id), -1)
                    apply()
                }
                onDone.invoke(true, "Logout")
            },{onDone.invoke(false, it)})
        )
    }
}