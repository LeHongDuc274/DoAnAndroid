package com.example.myapplication.core.api.response.user

import com.example.myapplication.core.api.response.user.UserResponse

data class LoginResponse(
    val status: Boolean,
    val data : UserResponse
)
