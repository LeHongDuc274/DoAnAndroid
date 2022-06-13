package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.core.api.response.LoginResponse
import com.example.myapplication.core.api.response.UserResponse
import com.example.myapplication.core.repo.MainRepository
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginViewModel(private val app: Application) : AndroidViewModel(app) {

    private val loginRepo = MainRepository.getInstance()

    fun login(loginId: String, password: String, onDone: (UserResponse?) -> Unit) {
        val paramObject = JSONObject()
        paramObject.put("login_id", loginId)
        paramObject.put("password", password)
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(paramObject.toString()).toString()
        )


        loginRepo.login(body) { res ->
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
}