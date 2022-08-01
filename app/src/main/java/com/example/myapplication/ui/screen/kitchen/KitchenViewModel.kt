package com.example.myapplication.ui.screen.kitchen

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.*
import com.example.myapplication.api.OrderService
import com.example.myapplication.api.ProductService
import com.example.myapplication.api.UserService
import com.example.myapplication.api.response.*
import com.example.myapplication.ui.model.OrderDetail
import com.example.myapplication.ui.model.ProductEntity
import com.example.myapplication.ui.viewmodel.BaseViewModel
import com.example.myapplication.utils.GsonUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class KitchenViewModel(private val app: Application) : BaseViewModel(app) {
    val listOrderDetails = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listPending = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listPreparing = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listComplete = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listDelivering = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listMessageRequesting = MutableStateFlow<MutableList<Message>>(mutableListOf())
    var onShowNotice: ((Message) -> Unit)? = null
    val isLoading = MutableStateFlow(false)
    var client = OkHttpClient()
    var ws: WebSocket? = null
    lateinit var request: Request

    fun initSocket() {
        Log.e("tagCheck","init")
        getListOrderDetails()
        if (connection) return
        request = Request.Builder().url(WS_URL)
            .addHeader(TOKEN, token).build()
        client = OkHttpClient.Builder().build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val order_details_Param = JSONObject()
                val paramProduct = JSONObject()
                val message_param = JSONObject()
                val channel = JSONObject()
                order_details_Param.put(COMMAND, SUBSCRIBE)
                channel.put("channel", "OrderDetailChannel")
                order_details_Param.put(IDENTIFIER, channel.toString())
                message_param.put(COMMAND, SUBSCRIBE)
                message_param.put(IDENTIFIER, Channel.MESSAGE_CHANNEL.channel)
                paramProduct.put(COMMAND, SUBSCRIBE)
                paramProduct.put(IDENTIFIER, Channel.PRODUCT_CHANNEL.channel)
                ws?.send(message_param.toString())
                ws?.send(paramProduct.toString())
                ws?.send(order_details_Param.toString())
                connection = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val res = GsonUtils.getGsonParser().fromJson(text, SocketMessage::class.java)

                when {
                    res.identifier.equals(Channel.ORDER_DETAIL_KITCHEN_CHANNEL.channel) -> {
                        handlerReceiOrderDetails(res.message)
                    }
                    res.identifier == Channel.MESSAGE_CHANNEL.channel -> {
                        handlerReceiNotice(res.message)
                    }
                    res.identifier == Channel.PRODUCT_CHANNEL.channel -> {
                        try {
                            val productEntity =
                                GsonUtils.getGsonParser()
                                    .fromJson(res.message, ProductEntity::class.java)
                            if (productEntity == null) return
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
                            } else {
                                val list = mutableListOf<ProductEntity>()
                                list.add(productEntity)
                                list.addAll(listProducts.value)
                                listProducts.value =  list
                            }
                        } catch (e: java.lang.Exception) {
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
                        initSocket()
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                connection = false
                client.dispatcher().cancelAll()
            }

        })
    }

    private fun handlerReceiNotice(message: String) {
        try {
            val data = GsonUtils.getGsonParser().fromJson(message, RequestMessage::class.java)
            data?.let {
                when (it.type) {
                    "create" -> {
                        it.data.firstOrNull()?.let { newMessage ->
                            onShowNotice?.invoke(newMessage)
                            listMessageRequesting.value =
                                (it.data + listMessageRequesting.value).toMutableList()
                        }
                    }
                    "accept" -> {
                        it.data.let { mess ->
                            listMessageRequesting.value =
                                (listMessageRequesting.value - mess).toMutableList()
                        }
                    }
                    else -> {
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handlerReceiOrderDetails(message: String) {
        try {
            val data = GsonUtils.getGsonParser().fromJson(message, OrderDetailsMessage::class.java)
            data?.let {
                when (it.type) {
                    "create" -> {
                        //add more to list
                        val newData = it.data
                        listOrderDetails.value += newData
                    }

                    "update" -> {
                        //find and update item
                        it.data.firstOrNull()?.let { detail ->
                            val index = listOrderDetails.value.indexOfFirst { detail.id == it.id }
                            if (index != -1) {
                                listOrderDetails.value.removeAt(index)
                                listOrderDetails.value.add(index, detail)
                            }
                        }
                    }
                    "delete" -> {
                        it.data.firstOrNull()?.let { detail ->
                            Log.e("tagXX", detail.toString())
                            listOrderDetails.value -= detail
                        }
                    }
                }
                setListFilter()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun finalizeSocket() {
        ws?.send("finishing")
        ws?.close(1001, null)
        client.dispatcher().executorService().shutdown()
        // client.connectionPool().evictAll()
    }

    fun getListOrderDetails() {
        isLoading.value = true
        val api = OrderService.createOrderApi(token)
        val res = api.getListOrderDetails()
        res.enqueue(MyCallback({
            listOrderDetails.value = it.toMutableList()
            setListFilter()
            isLoading.value = false
        },{
            isLoading.value = false
        }))
    }

    fun getListTableMessage() {
        isLoading.value = true
        val api = UserService.createUserApi(token)
        val res = api.getListMessageRequesting()
        res.enqueue(MyCallback({
            listMessageRequesting.value  = it.toMutableList()
            isLoading.value = false
        },{
            isLoading.value = false
        }))
    }

    fun doRequest(message: Message) {
        isLoading.value = true
        val api = UserService.createUserApi(token)
        val res = api.doRequestMessage(message.id)
        res.enqueue(MyCallback({
            isLoading.value = false
        },{
            isLoading.value = false
        }))
    }

    fun changeStatusProduct(productID: Int, onDone: (Boolean, String?, ProductEntity?) -> Unit) {
        val api = ProductService.createProductApi(token)
        val res = api.changeStatusProduct(productID)
        res.enqueue(MyCallback({
            onDone.invoke(true, null, it)
        },{
            onDone.invoke(false,it, null)
        }))
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