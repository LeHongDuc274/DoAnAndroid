package com.example.myapplication.core.api.response

data class UserResponse(
    val id: Int,
    val login_id: String,
    val display_name: String,
    val access_token: String,
    val status: Int,
    val role: Int
)