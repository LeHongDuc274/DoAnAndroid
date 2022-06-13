package com.example.myapplication.core.model

data class User(
    val user_id: Int,
    val role: Int,
    val name: String,
    val display_name: String = "",
    val status: Int = 0
)