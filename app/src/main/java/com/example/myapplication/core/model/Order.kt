package com.example.myapplication.core.model

data class Order(
    val id: Int = -1, val status: Int = -1, val user_id: Int = -1, var total_price: Int = 0,
    val order_details: ArrayList<OrderDetail> = arrayListOf()
)
