package com.example.myapplication.core.api.response

import com.example.myapplication.core.model.*

data class Result(
    val status: Boolean,
    var data: Any
)

data class CategoriesListResponse(
    val `data`: List<CategoryEntity>,
    val status: Boolean
)

data class ProductCreateRes(
    val `data`: ProductEntity,
    val status: Boolean
)

data class UsersRes(
    val `data`: List<User>,
    val status: Boolean
)

data class UserRes(
    val `data`: User,
    val status: Boolean
)

data class CategoryResponse(
    val `data`: CategoryEntity,
    val status: Boolean
)

data class OrderResponse(
    val status : Boolean,
    val `data` : Order
)

data class OrderDetailRes(
    val status: Boolean,
    val `data` : OrderDetail
)

data class OrderDetailsListRes(
    val status: Boolean,
    val `data` : List<OrderDetail>
)