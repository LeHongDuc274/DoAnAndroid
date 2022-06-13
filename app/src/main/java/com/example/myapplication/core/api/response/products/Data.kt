package com.example.myapplication.core.api.response.products

import okhttp3.MultipartBody

data class Data(
    val id: Int = -1,
    var name: String = "",
    var price: Int = 0,
    var content: String = "",
    var status: Int = 0,
    val category_id: Int = -1,
    val image: MultipartBody.Part? = null,
    var image_url : String = "",
    var created_at: String = "",
    var update_at : String = ""
)