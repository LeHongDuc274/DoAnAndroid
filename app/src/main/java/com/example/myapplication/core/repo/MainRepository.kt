package com.example.myapplication.core.repo

import com.example.myapplication.core.api.UserApi
import com.example.myapplication.core.api.response.user.LoginResponse
import okhttp3.RequestBody
import retrofit2.Call

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

}