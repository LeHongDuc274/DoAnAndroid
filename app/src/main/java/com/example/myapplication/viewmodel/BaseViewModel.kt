package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.core.api.OrderService
import com.example.myapplication.core.api.ProductService
import com.example.myapplication.core.api.UserService
import com.example.myapplication.core.api.response.MyResult
import com.example.myapplication.core.api.response.UserResponse
import com.example.myapplication.core.model.CategoryEntity
import com.example.myapplication.core.model.Order
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.core.utils.Utils
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
        productApi.getListCategories().enqueue(object : Callback<MyResult<List<CategoryEntity>>> {
            override fun onResponse(
                call: Call<MyResult<List<CategoryEntity>>>,
                response: Response<MyResult<List<CategoryEntity>>>
            ) {
                if (response.isSuccessful) {
                    val listdata = response.body()!!.data.toMutableList()
                    viewModelScope.launch {
                        listCategories.emit(listdata)
                    }
                } else {
                }
            }

            override fun onFailure(call: Call<MyResult<List<CategoryEntity>>>, t: Throwable) {
            }
        })

        productApi.getListProduct().enqueue(object : Callback<MyResult<List<ProductEntity>>> {
            override fun onResponse(call: Call<MyResult<List<ProductEntity>>>, response: Response<MyResult<List<ProductEntity>>>) {
                if (response.isSuccessful) {
                    val listdata = response.body()!!.data.toMutableList()
                    viewModelScope.launch {
                        listProducts.emit(listdata)
                        setListProductByCategory(categorySelected)
                    }
                } else {
                }
            }

            override fun onFailure(call: Call<MyResult<List<ProductEntity>>>, t: Throwable) {
            }
        })
    }

    fun updateOrderDetails(detail: OrderDetail, onDone: (Boolean, String, OrderDetail?) -> Unit) {
        val api = OrderService.createOrderApi(token)
        val res = api.updateOrderDetails(detail)
        res.enqueue(object : Callback<MyResult<OrderDetail>> {
            override fun onResponse(
                call: Call<MyResult<OrderDetail>>,
                response: Response<MyResult<OrderDetail>>
            ) {
                if (response.isSuccessful) {
                    onDone.invoke(true, "Update Order detail succes", response.body()!!.data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<MyResult<OrderDetail>>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }
        })
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
        res.enqueue(object : Callback<MyResult<Order?>> {
            override fun onResponse(call: Call<MyResult<Order?>>, response: Response<MyResult<Order?>>) {
                if (response.isSuccessful) {
                    onDone.invoke(true, "", response.body())
                } else {
                    onDone.invoke(false, response.message().toString(), null)
                }
            }

            override fun onFailure(call: Call<MyResult<Order?>>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }

        })
    }

    fun logout(password: String, onDone: (Boolean, String) -> Unit) {
        val api = UserService.createUserApi(token)
        //val body = createRequestBody(password)
        val paramObject = JSONObject()
        paramObject.put("password", password)
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(paramObject.toString()).toString()
        )
        val res = api.logout(body)
        res.enqueue(object : Callback<MyResult<UserResponse>> {
            override fun onResponse(call: Call<MyResult<UserResponse>>, response: Response<MyResult<UserResponse>>) {
                if (response.isSuccessful) {
                    val sharedPref = app.getSharedPreferences(
                        app.getString(R.string.shared_file_name), Context.MODE_PRIVATE
                    ) ?: return
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
                } else {
                    onDone.invoke(false, response.code().toString())
                }
            }

            override fun onFailure(call: Call<MyResult<UserResponse>>, t: Throwable) {
                onDone.invoke(false, t.message.toString())
            }

        })
    }
}