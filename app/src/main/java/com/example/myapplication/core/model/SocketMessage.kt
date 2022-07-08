package com.example.myapplication.core.api.response

import com.example.myapplication.core.model.OrderDetail

data class SocketMessage(
    var type: String = "",
    var identifier: String = "",
    var message: String  = ""
)

data class OrderDetailsMessage(
    val type: String,
    val data: List<OrderDetail>
)

data class RequestMessage(
    val type: String,
    val data: List<Message>
)

