package com.example.myapplication.core.model

data class OrderDetail(
    val id: Int,
    val userId: Int,
    val orderId: Int,
    val productId: Int,
    var amount: Int,
    var note: String = "",
    var status: Int
)
