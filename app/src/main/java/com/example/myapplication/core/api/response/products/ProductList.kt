package com.example.myapplication.core.api.response.products

import com.example.myapplication.core.model.ProductEntity

data class ProductList(
    val `data`: List<ProductEntity>,
    val status: Boolean
)