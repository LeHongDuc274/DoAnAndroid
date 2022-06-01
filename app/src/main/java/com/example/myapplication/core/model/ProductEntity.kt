package com.example.myapplication.core.model

data class ProductEntity(
    var name: String,
    val id: Int,
    var price: Int,
    var disciption: String,
    var status: Int = 0
) {
    var countOrder = 0
    var note = ""
}