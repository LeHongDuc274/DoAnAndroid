package com.example.myapplication.core.api

import com.example.myapplication.core.BASE_URL
import com.example.myapplication.core.api.response.OrderDetailRes
import com.example.myapplication.core.api.response.OrderDetailsListRes
import com.example.myapplication.core.api.response.OrderResponse
import com.example.myapplication.core.model.Order
import com.example.myapplication.core.model.OrderDetail
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface OrderApi {
    @POST("/orders/create")
    fun createOrder(@Body order : Order): Call<OrderResponse>


    @PUT("/order_details/update")
    fun updateOrderDetails(@Body order : OrderDetail): Call<OrderDetailRes>

    @POST("/order_details/create")
    fun createOrderDetails(@Body order : OrderDetail): Call<OrderDetailRes>

    @GET("/order_details/ordering")
    fun getListOrderDetails() : Call<OrderDetailsListRes>

    companion object {
        fun createOrderApi(token: String): OrderApi {
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(newRequest)
            }.build()
            return Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(OrderApi::class.java)
        }
    }
}