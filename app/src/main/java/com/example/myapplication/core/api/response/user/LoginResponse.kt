package com.example.myapplication.core.api.response.user


data class LoginResponse(
    val status: Boolean,
    val data : UserResponse,
    val error : String = ""
)
