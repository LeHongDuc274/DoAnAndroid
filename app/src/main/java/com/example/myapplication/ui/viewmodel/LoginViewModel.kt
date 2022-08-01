package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.api.UserService
import com.example.myapplication.api.response.MyResult
import com.example.myapplication.api.response.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginViewModel(private val app: Application) : AndroidViewModel(app) {
    val isLoading = MutableStateFlow(false)
    fun login(loginId: String, password: String, onDone: (UserResponse?) -> Unit) {
        isLoading.value = true
        val paramObject = JSONObject()
        paramObject.put("login_id", loginId)
        paramObject.put("password", password)
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(paramObject.toString()).toString()
        )
        val api = UserService.createLoginApi()
        val res = api.login(body)
        res.enqueue(object : Callback<MyResult<UserResponse>> {
            override fun onResponse(call: Call<MyResult<UserResponse>>, rep: Response<MyResult<UserResponse>>) {
                isLoading.value = false
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

            override fun onFailure(call: Call<MyResult<UserResponse>>, t: Throwable) {
                onDone.invoke(null)
                isLoading.value = false
            }
        })
    }

}
