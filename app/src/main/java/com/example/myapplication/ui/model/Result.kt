package com.example.myapplication.api.response

import com.example.myapplication.ui.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class Result(
    val status: Boolean,
    var data: Any
)

data class MyResult<T>(
    val status: Boolean,
    val message: String?,
    val data: T?
)

class MyCallback<T>(val onSuccess: (T) -> Unit, val onError: (String) -> Unit) :
    Callback<MyResult<T>> {
    override fun onResponse(call: Call<MyResult<T>>, response: Response<MyResult<T>>) {
        if (response.isSuccessful && response.body() != null) {
            if (response.body()!!.status) {
                val res = response.body()!!.data
                if (res != null) {
                    onSuccess.invoke(res)
                } else {
                    onError.invoke("")
                }
            } else {
                onError.invoke(response.body()!!.message ?: "")
            }
        } else {
            onError.invoke("Có lỗi xảy ra")
        }
    }

    override fun onFailure(call: Call<MyResult<T>>, t: Throwable) {
        onError.invoke("Có lỗi xảy ra: ${t.message}")
    }
}

data class TableOrdering(
    val display_name: String,
    val user_id: Int,
    val order: Order?
)

data class Message(
    val id: Int,
    val user_id: Int,
    val user_name: String,
    val content: String,
    val status: Int,
    val created_at: String
)

data class UserResponse(
    val id: Int,
    val login_id: String,
    val display_name: String,
    val access_token: String,
    val status: Int,
    val role: Int
)

data class ReportToday(
    val total_revenue: Int,
    val total_revenue_change: Float,
    val total_dish_order: Int,
    val total_dish_order_change: Float,
    val total_customer: Int,
    val total_customer_change: Float
)

data class RevenueReport(
    val time: String,
    val revenue: Int
)

data class ProductReport(
    val product: Int,
    val category_id: Int,
    val product_name: String,
    val count: Int,
    val revenue: Int
)


