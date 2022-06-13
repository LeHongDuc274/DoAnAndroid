package com.example.myapplication.core.api

import com.example.myapplication.core.BASE_URL
import com.example.myapplication.core.api.response.CategoriesList
import com.example.myapplication.core.api.response.ProductCreateRes
import com.example.myapplication.core.api.response.Result
import com.example.myapplication.core.api.response.products.ProductList
import com.example.myapplication.core.model.ProductEntity
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ProductApi {

    @GET("/products/index")
    fun getListProduct(): Call<ProductList>

    @Multipart
    @POST("/products/create")
    fun createProduct(
        @Part("name") name: RequestBody,
        @Part("content") content: RequestBody,
        @Part("price") price: RequestBody,
        @Part("status") status: RequestBody,
        @Part("category_id") category_id: RequestBody,
        @Part image: MultipartBody.Part
        ) : Call<ProductCreateRes>

    @PATCH("/products/edit")
    fun editProduct():Call<Result>

    @DELETE("/products/delete")
    fun deleteProduct(): Call<Response>

    @GET("/categories/index")
    fun getListCategories(): Call<CategoriesList>

    @POST("/categories/create")
    fun createCategory() : Call<Result>

    @PATCH("/categories/edit")
    fun editCategory():Call<Result>

    @DELETE("/categories/delete")
    fun deleteCategory(): Call<Response>

    companion object {
        fun createProductApi(token: String): ProductApi {
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(newRequest)
            }.build()
            return Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ProductApi::class.java)
        }
    }
}