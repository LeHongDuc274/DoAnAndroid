package com.example.myapplication.ui.screen.admin

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.core.*
import com.example.myapplication.api.OrderService
import com.example.myapplication.api.ProductService
import com.example.myapplication.api.UserService
import com.example.myapplication.api.response.*
import com.example.myapplication.ui.model.*
import com.example.myapplication.utils.GsonUtils
import com.example.myapplication.utils.RealPathUtil
import com.example.myapplication.ext.UserId
import com.example.myapplication.ext.clearTime
import com.example.myapplication.ext.createRequestBody
import com.example.myapplication.ext.formatVN
import com.example.myapplication.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class AdminViewModel(private val app: Application) : BaseViewModel(app) {
    private var role = -1
    val loading = MutableStateFlow(false)
    val listUser = MutableStateFlow<MutableList<User>>(mutableListOf())
    val listUserByRole = MutableStateFlow<MutableList<User>>(mutableListOf())
    val listTableActive = MutableStateFlow<MutableList<TableOrdering>>(mutableListOf())
    val listOrderDetailsByTable = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    val listMessageRequesting = MutableStateFlow<MutableList<Message>>(mutableListOf())
    val reportToday = MutableSharedFlow<ReportToday>()
    val listOrderHistory = MutableStateFlow<MutableList<Order>>(mutableListOf())
    val revenueLastWeek = MutableStateFlow<MutableList<RevenueReport>>(mutableListOf())
    val revenueAllTime = MutableStateFlow<MutableList<RevenueReport>>(mutableListOf())
    val revenuePeriodTime = MutableStateFlow<MutableList<RevenueReport>>(mutableListOf())
    val productReport = MutableStateFlow<MutableList<ProductReport>>(mutableListOf())
    val productReportByCategory = MutableStateFlow<MutableList<ProductReport>>(mutableListOf())
    var categoryIdReport = 0
    var roleSelected = Role.TABLE.code
    var tableIdSubscribe = app.UserId()
    val orderChannelSubscribe get() = String.format(ORDER_CHANNEL_FORMAT, tableIdSubscribe)
    var pageSelected = 0
    var startTimeRevenue = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, -7)
        clearTime()
    }
    var endTimeRevenue = Calendar.getInstance().apply { clearTime() }
    private var client = OkHttpClient()
    private var ws: WebSocket? = null
    lateinit var request: Request

    override fun initViewModel() {
        super.initViewModel()
        val sharedPref = app.getSharedPreferences(
            app.resources.getString(R.string.shared_file_name), Context.MODE_PRIVATE
        )
        role = sharedPref.getInt(app.resources.getString(R.string.key_role), -1)
        getUsers()
        initSocket()
    }

    fun subscribeChannel(userId: Int) {
        tableIdSubscribe = userId
        val param = JSONObject()
        param.put(COMMAND, SUBSCRIBE)
        param.put(IDENTIFIER, orderChannelSubscribe)
        ws?.send(param.toString())
    }

    fun initSocket() {
        client = OkHttpClient()
        request = Request.Builder().url(WS_URL)
            .addHeader(TOKEN, token).build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val param = JSONObject()
                param.put(COMMAND, SUBSCRIBE)
                param.put(IDENTIFIER, Channel.MESSAGE_CHANNEL.channel)
                val param2 = JSONObject()
                param2.put(COMMAND, SUBSCRIBE)
                param2.put(IDENTIFIER, Channel.PRODUCT_CHANNEL.channel)
                ws?.send(param.toString())
                ws?.send(param2.toString())
                subscribeChannel(tableIdSubscribe)
                connection = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val res = GsonUtils.getGsonParser().fromJson(text, SocketMessage::class.java)
                when {
                    res.identifier == orderChannelSubscribe && res.message == tableIdSubscribe.toString() -> {
                        getCurrentOrder(tableIdSubscribe) { b, mess, res ->
                            if (b && res != null) setListOrderDetailsByTable(
                                res.data?.order_details ?: mutableListOf()
                            )
                        }
                    }
                    res.identifier == Channel.MESSAGE_CHANNEL.channel && res.message == FLAG_UPDATE_ORDER -> {
                        getListTableOrder()
                    }

                    res.identifier == Channel.MESSAGE_CHANNEL.channel && res.message != FLAG_UPDATE_ORDER -> {
                        getListTableMessage()
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

    override fun onCleared() {
        super.onCleared()
        finalizeSocket()
    }

    fun finalizeSocket() {
        ws?.send("finishing")
        ws?.close(1001, null)
        client.dispatcher().executorService().shutdown()
        // client.connectionPool().evictAll()
    }

    fun createProduct(
        category_id: String,
        name: String,
        price: String,
        content: String,
        status: String,
        uri: Uri,
        onDone: (Boolean, String, ProductEntity?) -> Unit
    ) {
        loading.value = true
        val api = ProductService.createProductApi(token)
        val file = File(RealPathUtil.getRealPath(app, uri))
        val imageRequestBody =
            app.getContentResolver().openInputStream(uri)
                ?.let { ImageRequestBody(it, MediaType.get("image/*")) }

        val imageBody =
            MultipartBody.Part.createFormData("image", file.name, imageRequestBody)
        val res = api.createProduct(
            createRequestBody(name),
            createRequestBody(content),
            createRequestBody(price),
            createRequestBody(status),
            createRequestBody(category_id),
            imageBody
        )
        res.enqueue(MyCallback({
            onDone.invoke(true, "Tạo sản phẩm thành công", it)
            loading.value = false
        }, {
            onDone.invoke(false, it, null)
            loading.value = false
        }))
    }

    fun editProduct(
        id: String,
        category_id: String,
        name: String,
        price: String,
        content: String,
        status: String,
        uri: Uri? = null,
        onDone: (Boolean, String, ProductEntity?) -> Unit
    ) {
        loading.value = true
        val api = ProductService.createProductApi(token)
        var imageBody: MultipartBody.Part? = null
        if (uri != null) {
            val file = File(RealPathUtil.getRealPath(app, uri!!))
            val imageRequestBody =
                app.getContentResolver().openInputStream(uri)
                    ?.let { ImageRequestBody(it, MediaType.get("image/*")) }

            imageBody =
                MultipartBody.Part.createFormData("image", file.name, imageRequestBody)
        }

        val map: HashMap<String, RequestBody> = HashMap()
        map.apply {
            put("id", createRequestBody(id))
            put("name", createRequestBody(name))
            put("category_id", createRequestBody(category_id))
            put("content", createRequestBody(content))
            put("price", createRequestBody(price))
            put("status", createRequestBody(status))
        }
        val res = api.updateProduct(
            map,
            imageBody
        )
        res.enqueue(MyCallback({
            onDone.invoke(true, "Cập nhập sản phẩm thành công", it)
            loading.value = false
        }, {
            onDone.invoke(false, it, null)
            loading.value = false
        }))
    }

    fun getUsers() {
        val api = UserService.createUserApi(token)
        val res = api.getListUser()
        res.enqueue(MyCallback({
            listUser.value = it.toMutableList()
            getListUserByRole(roleSelected)
        }, {}))
    }

    fun getListUserByRole(roleSelected: Int) {
        this.roleSelected = roleSelected
        val list = listUser.value.filter { it.role == roleSelected }.toMutableList()
        viewModelScope.launch {
            listUserByRole.emit(list)
        }
    }

    fun createCategory(name: String, onDone: (Boolean, String, CategoryEntity?) -> Unit) {
        val api = ProductService.createProductApi(token)
        val res = api.createCategory(createRequestBody(name))
        res.enqueue(MyCallback({
            onDone.invoke(true, "Tạo thành công", it)
        }, {
            onDone.invoke(false, it, null)
        }))
    }

    fun createUser(
        loginId: String,
        displayName: String,
        password: String,
        passwordConfirm: String,
        status: String,
        role: String,
        onDone: (Boolean, String, User?) -> Unit
    ) {
        val api = UserService.createUserApi(token)
        val login_id = createRequestBody(loginId)
        val display_name = createRequestBody(displayName)
        val password_req = createRequestBody(password)
        val password_confirm = createRequestBody(passwordConfirm)
        val status_req = createRequestBody(status)
        val role_req = createRequestBody(role)
        val res = api.createUser(
            login_id,
            display_name,
            password_req,
            password_confirm,
            role_req,
            status_req
        )
        res.enqueue(MyCallback({
            var list = mutableListOf<User>()
            list.add(it)
            list.addAll(listUser.value)
            listUser.value = list
            getListUserByRole(roleSelected)
            onDone.invoke(true, "Tạo người dùng thành công", it)
        }, {
            onDone.invoke(false, it, null)
        }))
    }

    fun editUser(
        id: Int,
        displayName: String,
        status: String,
        role: String,
        onDone: (Boolean, String, User?) -> Unit
    ) {
        val api = UserService.createUserApi(token)
        val id_req = createRequestBody(id.toString())
        val display_name = createRequestBody(displayName)
        val status_req = createRequestBody(status)
        val role_req = createRequestBody(role)
        val res = api.editUser(id_req, display_name, role_req, status_req)
        res.enqueue(MyCallback({
            val list = mutableListOf<User>()
            val user = it
            list.addAll(listUser.value)
            val index = list.indexOfFirst { it.id == id }
            list.set(index, user)
            listUser.value = list
            getListUserByRole(roleSelected)
            onDone.invoke(true, "Thành công", it)
        }, {
            onDone.invoke(false, it, null)
        }))
    }

    fun getListTableOrder() {
        val api = OrderService.createOrderApi(token)
        val res = api.getListOrdering()
        res.enqueue(MyCallback({
            listTableActive.value = it
        }, {}))
    }

    fun getListTableMessage() {
        val api = UserService.createUserApi(token)
        val res = api.getListMessageRequesting()
        res.enqueue(MyCallback({
            listMessageRequesting.value = it.toMutableList()
        }, {}))
    }

    fun setListOrderDetailsByTable(mutableList: MutableList<OrderDetail>) {
        listOrderDetailsByTable.value = mutableList
    }

    fun completeOrder() {
        val api = OrderService.createOrderApi(token)
        val total_price = listOrderDetailsByTable.value.sumOf { it.total_price }
        val res = api.completeOrder(tableIdSubscribe, total_price)
        res.enqueue(MyCallback({
            listOrderDetailsByTable.value = mutableListOf()
        }, {}))
    }

    fun getReportToday() {
        val api = OrderService.createOrderApi(token)
        val res = api.getReportToday()
        res.enqueue(MyCallback({
            viewModelScope.launch {
                reportToday.emit(it)
            }
        }, {}))
    }

    fun getRevenueLastWeek() {
        val api = OrderService.createOrderApi(token)
        val res = api.getRevenueLastWeek()
        res.enqueue(MyCallback({
            revenueLastWeek.value = mutableListOf()
            revenueLastWeek.value = it.toMutableList()
        }, {}))
    }

    fun getRevenueAllTime() {
        val api = OrderService.createOrderApi(token)
        val res = api.getRevenueAllTime()
        res.enqueue(MyCallback({
            revenueAllTime.value = mutableListOf()
            revenueAllTime.value = it.toMutableList()
        }, {}))
    }

    fun getRevenuePeriodTime() {
        val api = OrderService.createOrderApi(token)
        val res = api.getRevenuePeriodTime(startTimeRevenue.formatVN(), endTimeRevenue.formatVN())
        res.enqueue(MyCallback({
            revenuePeriodTime.value = mutableListOf()
            revenuePeriodTime.value = it.toMutableList()
        }, {}))
    }

    fun getHistory(time : String = Calendar.getInstance().apply { clearTime() }.formatVN()){
        val api = OrderService.createOrderApi(token)
        val res = api.getHistory(time)
        res.enqueue(MyCallback({
            listOrderHistory.value = it.toMutableList()
        },{

        }))

    }

    fun getReportProduct(type: Int) {
        val api = OrderService.createOrderApi(token)
        val res = api.getProductReport(type)
        res.enqueue(MyCallback({
            productReport.value = mutableListOf()
            productReport.value = it.toMutableList()
            filterProductReportByCategory(categoryIdReport)
        }, {}))
    }
    fun deleteOrderDetails(id: Int) {
        val api = OrderService.createOrderApi(token)
        val res = api.deleteOrderDetails(id)
        res.enqueue(MyCallback(onSuccess = {

        }, {

        }))
    }
    fun filterProductReportByCategory(productId: Int) {
        categoryIdReport = productId
        if (productId == 0) {
            productReportByCategory.value = productReport.value
        } else {
            productReportByCategory.value = productReport.value.filter {
                it.category_id == categoryIdReport
            }.toMutableList()
        }
    }
}