package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.*
import com.example.myapplication.core.api.OrderService
import com.example.myapplication.core.api.response.OrderDetailsListRes
import com.example.myapplication.core.api.response.SocketResponse
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.utils.GsonUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KitchenViewModel(private val app: Application) : BaseViewModel(app) {
    val listOrderDetails = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listPending = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listPreparing = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listComplete = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())

    var client = OkHttpClient()
    var ws: WebSocket? = null
    lateinit var request: Request
    private var connecting = false

    fun initSocket() {
        Log.e("tagInit","ss")
        if (connecting) return
        request = Request.Builder().url(WS_URL)
            .addHeader(TOKEN, token).build()
        client = OkHttpClient.Builder().build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val param = JSONObject()
                param.put(COMMAND, SUBSCRIBE)
                param.put(IDENTIFIER, Channel.ORDER_DETAIL_KITCHEN_CHANNEL.channel)
                ws?.send(param.toString())
                connecting = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val res = GsonUtils.getGsonParser().fromJson(text, SocketResponse::class.java)
                if (res.identifier.equals(Channel.ORDER_DETAIL_KITCHEN_CHANNEL.channel)) {
                    getListOrderDetails()
                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                connecting = false
                client.dispatcher().cancelAll()
                viewModelScope.launch {
                    delay(2000)
                    if (!connecting) {
                        initSocket()
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.e("tagClose", reason.toString())
                connecting = false
                client.dispatcher().cancelAll()
            }

        })
    }

    fun finalizeSocket() {
        ws?.send("finishing")
        ws?.close(1001, null)
        client.dispatcher().executorService().shutdown()
        // client.connectionPool().evictAll()
    }

    fun getListOrderDetails() {
        val api = OrderService.createOrderApi(token)
        val res = api.getListOrderDetails()
        res.enqueue(object : Callback<OrderDetailsListRes> {
            override fun onResponse(
                call: Call<OrderDetailsListRes>,
                response: Response<OrderDetailsListRes>
            ) {
                if (response.isSuccessful) {
                    listOrderDetails.value = response.body()!!.data.toMutableList()
                    setListFilter()
                } else {

                }
            }

            override fun onFailure(call: Call<OrderDetailsListRes>, t: Throwable) {

            }

        })
    }

    fun setListFilter() {
        var list = listOrderDetails.value.filter {
            it.status == ItemStatus.PENDING.status
        }
        listPending.value = list.toMutableList()
        list = listOrderDetails.value.filter {
            it.status == ItemStatus.PREPARING.status
        }
        listPreparing.value = list.toMutableList()
        list = listOrderDetails.value.filter {
            it.status == ItemStatus.COMPLETED.status
        }
        listComplete.value = list.toMutableList()
    }
}