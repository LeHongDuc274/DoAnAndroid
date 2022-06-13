package com.example.myapplication.core.api.response

import com.example.myapplication.core.model.CategoriesEntity
import com.example.myapplication.core.model.ProductEntity

data class Result(
    val status : Boolean,
    var data: Any
)

data class CategoriesList(
    val `data`: List<CategoriesEntity>,
    val status: Boolean
)

data class ProductCreateRes(
    val `data`: ProductEntity,
    val status: Boolean
)
