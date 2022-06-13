package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.text.Editable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.R
import com.example.myapplication.core.api.ProductApi
import com.example.myapplication.core.api.response.ProductCreateRes
import com.example.myapplication.core.api.response.Result
import com.example.myapplication.core.model.ImageRequestBody
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.core.utils.RealPathUtil
import com.example.myapplication.ext.getContentType
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class AdminViewModel(private val app: Application) : AndroidViewModel(app) {
    private var role = -1
    private var accessToken = ""
    val loading = MutableStateFlow(false)
    init {
        val sharedPref = app.getSharedPreferences(
            app.resources.getString(R.string.shared_file_name), Context.MODE_PRIVATE
        )
        role = sharedPref.getInt(app.resources.getString(R.string.key_role), -1)
        sharedPref?.let {
            accessToken =
                it.getString(app.resources.getString(R.string.key_access_token), "").toString()
        }
    }

    fun createProduct(
        category_id: String,
        name: String,
        price: String,
        content: String,
        status: String,
        uri: Uri,
        onDone : (Boolean, String , ProductEntity?) -> Unit
    ) {
        loading.value = true
        val api = ProductApi.createProductApi(accessToken)
        val file = File(RealPathUtil.getRealPath(app,uri))
        val imageRequestBody =
            app.getContentResolver().openInputStream(uri)
                ?.let { ImageRequestBody(it,MediaType.get("image/*")) }

        val imageBody =
            MultipartBody.Part.createFormData("image", file.name, imageRequestBody)
        val res = api.createProduct(
            RequestBody.create(MediaType.parse("text/plain"),name),
            RequestBody.create(MediaType.parse("text/plain"),content),
            RequestBody.create(MediaType.parse("text/plain"),price),
            RequestBody.create(MediaType.parse("text/plain"),status),
            RequestBody.create(MediaType.parse("text/plain"),category_id),
            imageBody)
        res.enqueue(object : Callback<ProductCreateRes> {
            override fun onResponse(call: Call<ProductCreateRes>, response: Response<ProductCreateRes>) {
                loading.value = false
                if (response.isSuccessful) {
                  onDone.invoke(true,"",response.body()!!.data)
                } else {
                    onDone.invoke(false,"not ok",null)
                }
            }

            override fun onFailure(call: Call<ProductCreateRes>, t: Throwable) {
                loading.value = false
                onDone.invoke(false,t.message.toString(),null)
            }

        })

    }
}