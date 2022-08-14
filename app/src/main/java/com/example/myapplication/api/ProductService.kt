package com.example.myapplication.api

import com.example.myapplication.core.BASE_URL
import com.example.myapplication.api.response.*
import com.example.myapplication.ui.model.CategoryEntity
import com.example.myapplication.ui.model.ProductEntity
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


    @PATCH("/products/change_status")
    fun changeStatusProduct(@Query("id") id: Int): Call<MyResult<ProductEntity>>

    @GET("/categories/index")
    fun getListCategories(): Call<MyResult<List<CategoryEntity>>>

    @Multipart
    @POST("/categories/create")
    fun createCategory(@Part("name_type") name_type: RequestBody): Call<MyResult<CategoryEntity>>

    @PATCH("/categories/edit")
    fun editCategory(
        @Query("id") id: Int,
        @Query("name_type") name_type: String
    ): Call<MyResult<CategoryEntity>>

    @DELETE("/categories/delete")
    fun deleteCategory(@Query("id") id: Int): Call<MyResult<CategoryEntity>>

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