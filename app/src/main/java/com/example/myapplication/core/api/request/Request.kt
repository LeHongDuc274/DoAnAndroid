package com.example.myapplication.core.api.request

import com.example.myapplication.core.model.OrderDetail

data class OrderWithOrderDetails(
    var status : Int ,
    var total_price: Int,
    var order_details : List<OrderDetail>

)