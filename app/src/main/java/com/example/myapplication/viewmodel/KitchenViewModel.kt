package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewModelScope
import com.example.myapplication.MyApp
import com.example.myapplication.core.*
import com.example.myapplication.core.api.OrderService
import com.example.myapplication.core.api.UserService
import com.example.myapplication.core.api.response.Message
import com.example.myapplication.core.api.response.MyResult
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
    val listDelivering = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listMessageRequesting = MutableStateFlow<MutableList<Message>>(mutableListOf())
    var client = OkHttpClient()
    var ws: WebSocket? = null
    lateinit var request: Request
    private var connecting = false

    fun initSocket(onMessge: (() -> Unit)? = null) {
        if (connecting) return
        request = Request.Builder().url(WS_URL)
            .addHeader(TOKEN, token).build()
        client = OkHttpClient.Builder().build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val order_details_Param = JSONObject()
                order_details_Param.put(COMMAND, SUBSCRIBE)
                val channel = JSONObject()
                channel.put("channel", "OrderDetailChannel")
                order_details_Param.put(IDENTIFIER, channel.toString())
                ws?.send(order_details_Param.toString())
                val message_param = JSONObject()
                message_param.put(COMMAND, SUBSCRIBE)
                message_param.put(IDENTIFIER, Channel.MESSAGE_CHANNEL.channel)
                ws?.send(message_param.toString())
                connecting = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val res = GsonUtils.getGsonParser().fromJson(text, SocketResponse::class.java)
                when {
                    res.identifier.equals(Channel.ORDER_DETAIL_KITCHEN_CHANNEL.channel) -> {
                        getListOrderDetails()
                    }
                    res.identifier == Channel.MESSAGE_CHANNEL.channel && res.message == FLAG_UPDATE_MESSAGE -> {
                        getListTableMessage()
                    }
                    res.identifier == Channel.MESSAGE_CHANNEL.channel && res.message == FLAG_NEW_MESSAGE -> {
                        onMessge?.invoke()
                    }
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
        res.enqueue(object : Callback<MyResult<List<OrderDetail>>> {
            override fun onResponse(
                call: Call<MyResult<List<OrderDetail>>>,
                response: Response<MyResult<List<OrderDetail>>>
            ) {
                if (response.isSuccessful) {
                    listOrderDetails.value = response.body()!!.data.toMutableList()
                    setListFilter()
                } else {

                }
            }

            override fun onFailure(call: Call<MyResult<List<OrderDetail>>>, t: Throwable) {

            }

        })
    }

    fun getListTableMessage() {
        val api = UserService.createUserApi(token)
        val res = api.getListMessageRequesting()
        res.enqueue(object : Callback<MyResult<List<Message>>> {
            override fun onResponse(
                call: Call<MyResult<List<Message>>>,
                response: Response<MyResult<List<Message>>>
            ) {
                if (response.isSuccessful) {
                    val list = response.body()!!.data
                    listMessageRequesting.value = list.toMutableList()
                } else {

                }
            }

            override fun onFailure(call: Call<MyResult<List<Message>>>, t: Throwable) {

            }

        })
    }

    fun setListFilter() {
        val mapDetailsByStatus = listOrderDetails.value.groupBy {
            it.status
        }
        listPending.value =
            mapDetailsByStatus[ItemStatus.PENDING.status]?.toMutableList() ?: mutableListOf()

        listPreparing.value =
            mapDetailsByStatus[ItemStatus.PREPARING.status]?.toMutableList() ?: mutableListOf()

        listComplete.value =
            mapDetailsByStatus[ItemStatus.COMPLETED.status]?.toMutableList() ?: mutableListOf()

        listDelivering.value =
            mapDetailsByStatus[ItemStatus.DELIVERING.status]?.toMutableList() ?: mutableListOf()
    }
}