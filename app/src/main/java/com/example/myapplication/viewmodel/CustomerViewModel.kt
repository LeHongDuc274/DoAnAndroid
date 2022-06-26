package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.*
import com.example.myapplication.core.api.OrderService
import com.example.myapplication.core.api.response.OrderDetailRes
import com.example.myapplication.core.api.response.OrderResponse
import com.example.myapplication.core.api.response.SocketResponse
import com.example.myapplication.core.model.Order
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.core.utils.GsonUtils
import com.example.myapplication.ext.UserId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerViewModel(private val app: Application) : BaseViewModel(app) {

    val listOrderDetails = MutableStateFlow<List<OrderDetail>>(listOf())
    var order: Order = Order()
    private var client = OkHttpClient()
    private var ws: WebSocket? = null
    private var request: Request? = null
    private var connection = false
    var orderChannelIdent =
        String.format("{\"channel\":\"OrderChannel\", \"user_id\": \"%d\"}", app.UserId())

    fun initSocket(onOrderDone: (Boolean) -> Unit) {
        client = OkHttpClient()
        request = Request.Builder().url(WS_URL)
            .addHeader(TOKEN, token).build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val param = JSONObject()
                param.put(COMMAND, SUBSCRIBE)
                param.put(IDENTIFIER, orderChannelIdent)
                ws?.send(param.toString())
                connection = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val res = GsonUtils.getGsonParser().fromJson(text, SocketResponse::class.java)
                when (res.identifier) {
                    orderChannelIdent -> {
                        getCurrentOrder(app.UserId()) { b, str, response ->
                            response?.let {
                                order = it.data ?: Order()
                                setListOrder(response.data?.order_details ?: mutableListOf())
                                onOrderDone.invoke(order.id != -1)
                            }
//                            }
                        }
                    }
                    Channel.PRODUCT_CHANNEL.channel -> {
                        //reload product
                    }

                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                connection = false
                client.dispatcher().cancelAll()
                viewModelScope.launch {
                    delay(2000)
                    if (!connection) {
                        initSocket {}
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            }

        })
    }

    fun finalizeSocket() {
        ws?.send("finishing")
        ws?.close(1001, null)
        client.dispatcher().executorService().shutdown()
        // client.connectionPool().evictAll()
    }

    fun setListOrder(list: MutableList<OrderDetail>) {
        viewModelScope.launch {
            listOrderDetails.emit(list)
        }
    }

    fun createOrder(onDone: (Boolean, String, Order?) -> Unit) {
        val api = OrderService.createOrderApi(token)
        val res = api.createOrder(order)
        res.enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()!!.data!!
                    order = data
                    listOrderDetails.value = data.order_details
                    onDone.invoke(true, "Succes", data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }

        })
    }

    fun createOrderDetail(detail: OrderDetail, onDone: (Boolean, String, OrderDetail?) -> Unit) {
        val api = OrderService.createOrderApi(token)
        val res = api.createOrderDetails(detail)
        res.enqueue(object : Callback<OrderDetailRes> {
            override fun onResponse(
                call: Call<OrderDetailRes>,
                response: Response<OrderDetailRes>
            ) {
                if (response.isSuccessful) {
                    onDone.invoke(true, "Create detail succes", response.body()!!.data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<OrderDetailRes>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }
        })
    }

    fun subTotal(): Int {
        return listOrderDetails.value.sumOf { detail ->
            val product = listProducts.value.firstOrNull { p ->
                p.id == detail.product_id
            }
            val price = product?.price ?: 0
            detail.amount * price
        }
    }

}

