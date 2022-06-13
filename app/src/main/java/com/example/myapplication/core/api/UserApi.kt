package com.example.myapplication.core.api

import com.example.myapplication.core.BASE_URL
import com.example.myapplication.core.api.response.LoginResponse
import com.example.myapplication.core.api.response.Result
import com.example.myapplication.core.api.response.UserResponse
import com.example.myapplication.core.model.User
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface UserApi {

    @POST("/authenticate")
    fun login(@Body requestBody: RequestBody): Call<LoginResponse>

    @POST("/users/create")
    fun createUser(
        @Field("login_id") login_id: String,
        @Field("display_name") display_name: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
        @Field("role") role: Int,
        @Field("status") status: String,
    ): Call<UserResponse>

    @GET("/users/index")
    fun getListUser() : Call<Result>

    @GET("/users/tables")
    fun getListTable() : Call<Result>

    @PATCH("/users/edit")
    fun editUser(): Call<Result>

    @DELETE("/users/delete")
    fun deleteUser() : Call<Result>


    companion object {
        fun createLoginApi(): UserApi {
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(newRequest)
            }.build()
            return Retrofit.Builder().baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(UserApi::class.java)
        }

        fun createUserApi(token: String): UserApi {
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
                .build().create(UserApi::class.java)
        }
    }
}