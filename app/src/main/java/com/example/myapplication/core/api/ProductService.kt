package com.example.myapplication.core.api

import com.example.myapplication.core.BASE_URL
import com.example.myapplication.core.api.response.CategoriesListResponse
import com.example.myapplication.core.api.response.CategoryResponse
import com.example.myapplication.core.api.response.ProductCreateRes
import com.example.myapplication.core.api.response.Result
import com.example.myapplication.core.api.response.products.ProductList
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ProductService {

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
    ): Call<ProductCreateRes>

    @Multipart
    @PATCH("/products/edit")
    fun updateProduct(
        @PartMap() partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part? = null
    ): Call<ProductCreateRes>


    @DELETE("/products/delete")
    fun deleteProduct(): Call<Response>

    @GET("/categories/index")
    fun getListCategories(): Call<CategoriesListResponse>

    @Multipart
    @POST("/categories/create")
    fun createCategory(@Part("name_type") name_type : RequestBody): Call<CategoryResponse>

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