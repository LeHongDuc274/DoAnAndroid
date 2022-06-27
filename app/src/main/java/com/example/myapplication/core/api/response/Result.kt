package com.example.myapplication.core.api.response

import com.example.myapplication.core.model.*

data class Result(
    val status: Boolean,
    var data: Any
)

data class MyResult<T>(
    val status: Boolean,
    val data : T
)

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
