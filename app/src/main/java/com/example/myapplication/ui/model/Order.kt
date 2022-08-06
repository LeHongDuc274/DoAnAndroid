package com.example.myapplication.ui.model

data class Order(
    val id: Int = -1, val status: Int = -1, val user_id: Int = -1, var total_price: Int = 0,
    val user_name: String = "", val created_at : String = "",
    val order_details: ArrayList<OrderDetail> = arrayListOf()
)
