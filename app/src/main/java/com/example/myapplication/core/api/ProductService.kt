package com.example.myapplication.core.api

import com.example.myapplication.core.BASE_URL
import com.example.myapplication.core.api.response.*
import com.example.myapplication.core.model.CategoryEntity
import com.example.myapplication.core.model.ProductEntity
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ProductService {

    @GET("/products/index")
    fun getListProduct(): Call<MyResult<List<ProductEntity>>>

    @Multipart
    @POST("/products/create")
    fun createProduct(
        @Part("name") name: RequestBody,
        @Part("content") content: RequestBody,
        @Part("price") price: RequestBody,
        @Part("status") status: RequestBody,
        @Part("category_id") category_id: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<MyResult<ProductEntity>>

    @Multipart
    @PATCH("/products/edit")
    fun updateProduct(
        @PartMap() partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part? = null
    ): Call<MyResult<ProductEntity>>


    @DELETE("/products/delete")
    fun deleteProduct(): Call<Response>

    @GET("/categories/index")
    fun getListCategories(): Call<MyResult<List<CategoryEntity>>>

    @Multipart
    @POST("/categories/create")
    fun createCategory(@Part("name_type") name_type : RequestBody): Call<MyResult<CategoryEntity>>

    @PATCH("/categories/edit")
    fun editCategory(): Call<Result>

    @DELETE("/categories/delete")
    fun deleteCategory(): Call<Response>

    companion object {
        fun createProductApi(token: String): ProductService {
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
                .build().create(ProductService::class.java)
        }
    }
}