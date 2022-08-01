package com.example.myapplication.api

import com.example.myapplication.core.BASE_URL
import com.example.myapplication.api.response.*
import com.example.myapplication.ui.model.Order
import com.example.myapplication.ui.model.OrderDetail
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface OrderService {
    @POST("/orders/create")
    fun createOrder(@Body order: Order): Call<MyResult<Order>>

    @PUT("/order_details/update")
    fun updateOrderDetails(@Body order: OrderDetail): Call<MyResult<OrderDetail>>

    @POST("/order_details/create")
    fun createOrderDetails(@Body order: OrderDetail): Call<MyResult<OrderDetail>>

    @GET("/order_details/ordering")
    fun getListOrderDetails(): Call<MyResult<List<OrderDetail>>>

    @GET("/orders/ordering")
    fun getListOrdering(): Call<MyResult<MutableList<TableOrdering>>>

    @DELETE("/order_details/delete")
    fun deleteOrderDetails(@Query("id") id: Int): Call<MyResult<String>>

    @GET("/orders/order")
    fun getCurrentOrder(@Query("user_id") user_id: Int = -1): Call<MyResult<Order?>>

    @PATCH("/orders/complete")
    fun completeOrder(
        @Query("user_id") user_id: Int,
        @Query("total_price") total_price: Int
    ): Call<MyResult<Boolean>>

    @GET("/reports/report_today")
    fun getReportToday(): Call<MyResult<ReportToday>>

    @GET("/reports/revenue_last_week")
    fun getRevenueLastWeek(): Call<MyResult<List<RevenueReport>>>

    @GET("/reports/revenue_all_time")
    fun getRevenueAllTime(): Call<MyResult<List<RevenueReport>>>

    @GET("/reports/revenue_period_time")
    fun getRevenuePeriodTime(
        @Query("start") start: String,
        @Query("end") end: String
    ): Call<MyResult<List<RevenueReport>>>

    @GET("/reports/product")
    fun getProductReport(@Query("type") type: Int): Call<MyResult<List<ProductReport>>>

    companion object {
        fun createOrderApi(token: String): OrderService {
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
                .build().create(OrderService::class.java)
        }
    }
}