package com.example.myapplication.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.*
import com.example.myapplication.core.api.OrderService
import com.example.myapplication.core.api.UserService
import com.example.myapplication.core.api.response.*
import com.example.myapplication.core.model.Order
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.model.ProductEntity
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
import java.lang.Exception

class CustomerViewModel(private val app: Application) : BaseViewModel(app) {

    val listOrderDetails = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    var order: Order = Order()
    private var client = OkHttpClient()
    var ws: WebSocket? = null
    private var request: Request? = null
    val arletMessage = MutableStateFlow("")
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
                val param2 = JSONObject()
                param2.put(COMMAND, SUBSCRIBE)
                param2.put(IDENTIFIER, Channel.PRODUCT_CHANNEL.channel)
                ws?.send(param.toString())
                ws?.send(param2.toString())
                connection = true
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onMessage(webSocket: WebSocket, text: String) {
                val res = GsonUtils.getGsonParser().fromJson(text, SocketMessage::class.java)
                when (res.identifier) {
                    orderChannelIdent -> {
                        getCurrentOrder(app.UserId()) { b, str, response ->
                            response?.let {
                                order = it.data ?: Order()
                                setListOrder(response.data?.order_details ?: mutableListOf())
                                onOrderDone.invoke(order.id != -1)
                            }
                        }
                    }
                    Channel.PRODUCT_CHANNEL.channel -> {
                        try {
                            val productEntity =
                                GsonUtils.getGsonParser()
                                    .fromJson(res.message, ProductEntity::class.java)
                            if (productEntity == null) return
                            if (productEntity.status == 1) checkValidProductInCart(productEntity)
                            val raw_image = productEntity.image_url
                            val url = BASE_URL + raw_image.splitToSequence("?").first()
                            productEntity.image_url = url
                            val index = listProducts.value.indexOfLast {
                                it.id == productEntity.id
                            }
                            if (index != -1) {
                                val list = mutableListOf<ProductEntity>()
                                list.addAll(listProducts.value)
                                list.removeAt(index)
                                list.add(index, productEntity)
                                listProducts.value = list
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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

    private fun checkValidProductInCart(productEntity: ProductEntity) {
        val index = listOrderDetails.value.indexOfLast {
            it.product_id == productEntity.id
        }
        if (index != -1) {
            val detail = listOrderDetails.value.get(index)
            when {
                (detail.id != -1 && detail.status < 1) -> {
                    deleteOrderDetails(detail.id)
                    arletMessage.value = listProducts.value.findLast {
                        productEntity.id == it.id
                    }?.name ?: ""
                }
                (detail.id == -1) -> {
                    val list = mutableListOf<OrderDetail>().apply {
                        addAll(listOrderDetails.value)
                        removeAt(index)
                    }
                    listOrderDetails.value = list
                    arletMessage.value = listProducts.value.findLast {
                        productEntity.id == it.id
                    }?.name ?: ""
                }
            }
        }
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

    fun deleteOrderDetails(id: Int) {
        val api = OrderService.createOrderApi(token)
        val res = api.deleteOrderDetails(id)
        res.enqueue(object : Callback<MyResult<String>> {
            override fun onResponse(
                call: Call<MyResult<String>>,
                response: Response<MyResult<String>>
            ) {

            }

            override fun onFailure(call: Call<MyResult<String>>, t: Throwable) {
            }

        })
    }

    fun createOrder(onDone: (Boolean, String, Order?) -> Unit) {
        val api = OrderService.createOrderApi(token)
        val res = api.createOrder(order)
        res.enqueue(object : Callback<MyResult<Order?>> {
            override fun onResponse(
                call: Call<MyResult<Order?>>,
                response: Response<MyResult<Order?>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()!!.data!!
                    order = data
                    listOrderDetails.value = data.order_details
                    onDone.invoke(true, "Thành công", null)
                } else {
                    onDone.invoke(false, "Thất bại , thử lại sau", null)
                }
            }

            override fun onFailure(call: Call<MyResult<Order?>>, t: Throwable) {
                onDone.invoke(false, "Thất bại , thử lại sau", null)
            }

        })
    }

    fun createOrderDetail(
        detail: OrderDetail,
        onDone: (Boolean, String, OrderDetail?) -> Unit
    ) {
        val api = OrderService.createOrderApi(token)
        val res = api.createOrderDetails(detail)
        res.enqueue(object : Callback<MyResult<OrderDetail>> {
            override fun onResponse(
                call: Call<MyResult<OrderDetail>>,
                response: Response<MyResult<OrderDetail>>
            ) {
                if (response.isSuccessful) {
                    onDone.invoke(true, "Thành công", response.body()!!.data)
                } else {
                    onDone.invoke(false, "Thất bại , thử lại sau", null)
                }
            }

            override fun onFailure(call: Call<MyResult<OrderDetail>>, t: Throwable) {
                onDone.invoke(false, "Thất bại , thử lại sau", null)
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
            override fun onResponse(
                call: Call<MyResult<Message>>,
                response: Response<MyResult<Message>>
            ) {
                if (response.isSuccessful) {
                    onDone.invoke("Đang xử lí")
                } else {
                    onDone.invoke("Thất bại , thử lại sau")
                }
            }

            override fun onFailure(call: Call<MyResult<Message>>, t: Throwable) {
                onDone.invoke("Thất bại , thử lại sau")
            }

        })
    }

}

