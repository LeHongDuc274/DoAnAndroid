package com.example.myapplication.core.api

import com.example.myapplication.core.BASE_URL
import com.example.myapplication.core.api.response.*
import com.example.myapplication.core.api.response.user.LoginResponse
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

    @Multipart
    @POST("/users/create")
    fun createUser(
        @Part("login_id") login_id: RequestBody,
        @Part("display_name") display_name: RequestBody,
        @Part("password") password: RequestBody,
        @Part("password_confirmation") password_confirmation: RequestBody,
        @Part("role") role: RequestBody,
        @Part("status") status: RequestBody,
    ): Call<UserRes>

    @GET("/users/index")
    fun getListUser(): Call<UsersRes>

    @GET("/users/tables")
    fun getListTable(): Call<UsersRes>

    @Multipart
    @PATCH("/users/edit")
    fun editUser(
        @Part("id") id: RequestBody,
        @Part("display_name") display_name: RequestBody,
        @Part("role") role: RequestBody,
        @Part("status") status: RequestBody
        ): Call<UserRes>


    @DELETE("/users/delete")
    fun deleteUser(): Call<Result>


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