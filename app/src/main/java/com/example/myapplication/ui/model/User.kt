package com.example.myapplication.ui.model

data class User(
    val id: Int,
    val role: Int,
    val login_id: String,
    val display_name: String = "",
    val status: Int = 0
)