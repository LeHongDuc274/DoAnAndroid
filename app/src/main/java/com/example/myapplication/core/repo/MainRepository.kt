package com.example.myapplication.core.repo

import com.example.myapplication.core.api.UserApi
import com.example.myapplication.core.api.response.LoginResponse
import com.example.myapplication.core.api.response.UserResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field

class MainRepository {
    companion object {
        private var instance: MainRepository? = null
        fun getInstance(): MainRepository {
            if (instance == null) {
                synchronized(this) {
                    instance = MainRepository()
                }
            }
            return instance!!
        }
    }

    fun login(body: RequestBody, callback: (Call<LoginResponse>) -> Unit) {
        val api = UserApi.createLoginApi().login(body)
        callback.invoke(api)
    }

    fun createUser(
        token: String,
        login_id: String,
        display_name: String,
        password: String,
        password_confirmation: String,
        role: Int,
        status: String,
        callback: (Call<UserResponse>) -> Unit
    ) {
        val api = UserApi.createUserApi(token).createUser(
            login_id, display_name, password, password_confirmation, role, status
        )
        callback.invoke(api)
    }
}