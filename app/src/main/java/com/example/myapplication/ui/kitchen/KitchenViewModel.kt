package com.example.myapplication.ui.kitchen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.core.ItemStatus
import com.example.myapplication.core.api.OrderApi
import com.example.myapplication.core.api.response.OrderDetailsListRes
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.ext.AccessToken
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KitchenViewModel(private val app: Application) : AndroidViewModel(app) {
    private val token : String = app.AccessToken()
    val listOrderDetails = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listPending = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listPreparing = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listComplete = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    init {
        getListOrderDetails()
    }

    private fun getListOrderDetails() {
        val api = OrderApi.createOrderApi(token)
        val res = api.getListOrderDetails()
        res.enqueue(object: Callback<OrderDetailsListRes>{
            override fun onResponse(
                call: Call<OrderDetailsListRes>,
                response: Response<OrderDetailsListRes>
            ) {
                if (response.isSuccessful){
                    listOrderDetails.value = response.body()!!.data.toMutableList()
                    setListFilter()
                } else {

                }
            }

            override fun onFailure(call: Call<OrderDetailsListRes>, t: Throwable) {

            }

        })
    }

    fun setListFilter(){
        val list = listOrderDetails.value.filter {
            it.status == ItemStatus.PENDING.status
        }
        listPending.value = list.toMutableList()
    }
}