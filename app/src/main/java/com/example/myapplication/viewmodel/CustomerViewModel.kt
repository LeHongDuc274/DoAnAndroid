package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.*
import com.example.myapplication.core.api.OrderService
import com.example.myapplication.core.api.UserService
import com.example.myapplication.core.api.response.*
import com.example.myapplication.core.model.Order
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.utils.GsonUtils
import com.example.myapplication.ext.UserId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
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
        res.enqueue(object : Callback<MyResult<Order?>> {
            override fun onResponse(call: Call<MyResult<Order?>>, response: Response<MyResult<Order?>>) {
                if (response.isSuccessful) {
                    val data = response.body()!!.data!!
                    order = data
                    listOrderDetails.value = data.order_details
                    onDone.invoke(true, "Succes", data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<MyResult<Order?>>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }

        })
    }

    fun createOrderDetail(detail: OrderDetail, onDone: (Boolean, String, OrderDetail?) -> Unit) {
        val api = OrderService.createOrderApi(token)
        val res = api.createOrderDetails(detail)
        res.enqueue(object : Callback<MyResult<OrderDetail>> {
            override fun onResponse(
                call: Call<MyResult<OrderDetail>>,
                response: Response<MyResult<OrderDetail>>
            ) {
                if (response.isSuccessful) {
                    onDone.invoke(true, "Create detail succes", response.body()!!.data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<MyResult<OrderDetail>>, t: Throwable) {
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

    fun createMessageRequest(content: String, onDone: (String) -> Unit) {
        val api = UserService.createUserApi(token)
        val paramObject = JSONObject()
        paramObject.put("content", content)
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            JSONObject(paramObject.toString()).toString()
        )
        val res = api.createMessage(body)
        res.enqueue(object : Callback<MyResult<Message>> {
            override fun onResponse(call: Call<MyResult<Message>>, response: Response<MyResult<Message>>) {
                if (response.isSuccessful) {
                    onDone.invoke("requesting")
                } else {
                    onDone.invoke("error" + response.code())
                }
            }

            override fun onFailure(call: Call<MyResult<Message>>, t: Throwable) {
                onDone.invoke("errors" + t.message.toString())
            }

        })
    }

}

