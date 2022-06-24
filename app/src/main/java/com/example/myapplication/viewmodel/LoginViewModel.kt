package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.core.api.UserService
import com.example.myapplication.core.api.response.user.LoginResponse
import com.example.myapplication.core.api.response.user.UserResponse
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginViewModel(private val app: Application) : AndroidViewModel(app) {

    fun login(loginId: String, password: String, onDone: (UserResponse?) -> Unit) {
        val paramObject = JSONObject()
        paramObject.put("login_id", loginId)
        paramObject.put("password", password)
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(paramObject.toString()).toString()
        )
        val api = UserService.createLoginApi()
        val res = api.login(body)
        res.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, rep: Response<LoginResponse>) {
                if (rep.isSuccessful) {
                    rep.body()?.let {
                        if (it.status) {
                            onDone.invoke(it.data)
                        } else {
                            onDone.invoke(null)
                        }
                    }
                } else {
                    onDone.invoke(null)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onDone.invoke(null)
            }
        })
    }
}
