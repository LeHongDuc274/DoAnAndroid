package com.example.myapplication.core.model

import okhttp3.MultipartBody

data class ProductEntity(
    var id: Int = -1,
    var name: String = "",
    var content: String = "",
    var price: Int = 0,
    var status: Int = 0,
    var category_id: Int = -1,
    var image: MultipartBody.Part? = null,
    var image_url : String = "",
    var created_at: String = "",
    var update_at : String = ""
) {
    var countOrder = 0
    var note = ""
}