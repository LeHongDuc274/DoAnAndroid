package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.core.*
import com.example.myapplication.core.api.OrderService
import com.example.myapplication.core.api.ProductService
import com.example.myapplication.core.api.UserService
import com.example.myapplication.core.api.response.*
import com.example.myapplication.core.model.*
import com.example.myapplication.core.utils.GsonUtils
import com.example.myapplication.core.utils.RealPathUtil
import com.example.myapplication.ext.UserId
import com.example.myapplication.ext.createRequestBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class AdminViewModel(private val app: Application) : BaseViewModel(app) {
    private var role = -1
    val loading = MutableStateFlow(false)
    val listUser = MutableStateFlow<MutableList<User>>(mutableListOf())
    val listUserByRole = MutableStateFlow<MutableList<User>>(mutableListOf())
    val listTableActive = MutableStateFlow<MutableList<TableOrdering>>(mutableListOf())
    val listOrder = MutableStateFlow<MutableList<OrderResponse>>(mutableListOf())
    val listOrderDetailsByTable = MutableStateFlow<MutableList<OrderDetail>>(mutableListOf())
    var roleSelected = Role.TABLE.code
    var tableIdSubscribe = app.UserId()
    val orderChannelSubscribe get() = String.format(ORDER_CHANNEL_FORMAT, tableIdSubscribe)
    var pageSelected = 0

    private var client = OkHttpClient()
    private var ws: WebSocket? = null
    lateinit var request: Request
    private var connecting = false

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
        Log.e("tagSub", orderChannelSubscribe)
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
                ws?.send(param.toString())
                connecting = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val res = GsonUtils.getGsonParser().fromJson(text, SocketResponse::class.java)
                Log.e("tagX", text.toString())
                when (res.identifier) {
                    orderChannelSubscribe -> {
                        getCurrentOrder(tableIdSubscribe) { b, mess, res ->
                            if (b && res != null) setListOrderDetailsByTable(
                                res.data?.order_details ?: mutableListOf()
                            )
                        }
                    }
                    Channel.MESSAGE_CHANNEL.channel -> {
                        getListTableOrder()
                    }
                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                Log.e("tagXFail", t.toString())
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
                Log.e("tagX", reason.toString())
                connecting = false
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
        res.enqueue(object : Callback<ProductCreateRes> {
            override fun onResponse(
                call: Call<ProductCreateRes>,
                response: Response<ProductCreateRes>
            ) {
                loading.value = false
                if (response.isSuccessful) {
                    onDone.invoke(true, "", response.body()!!.data)
                } else {
                    onDone.invoke(false, "not ok", null)
                }
            }

            override fun onFailure(call: Call<ProductCreateRes>, t: Throwable) {
                loading.value = false
                onDone.invoke(false, t.message.toString(), null)
            }
        })
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
        Log.e("tagDataNew", "$id  $name")
        val res = api.updateProduct(
            map,
            imageBody
        )
        res.enqueue(object : Callback<ProductCreateRes> {
            override fun onResponse(
                call: Call<ProductCreateRes>,
                response: Response<ProductCreateRes>
            ) {
                loading.value = false
                if (response.isSuccessful) {
                    onDone.invoke(true, "", response.body()!!.data)
                } else {
                    onDone.invoke(false, "not ok", null)
                }
            }

            override fun onFailure(call: Call<ProductCreateRes>, t: Throwable) {
                loading.value = false
                onDone.invoke(false, t.message.toString(), null)
            }
        })
    }

    fun getUsers() {
        val api = UserService.createUserApi(token)
        val res = api.getListUser()
        res.enqueue(object : Callback<UsersRes> {
            override fun onResponse(call: Call<UsersRes>, response: Response<UsersRes>) {
                if (response.isSuccessful) {
                    listUser.value = response.body()!!.data.toMutableList()
                    getListUserByRole(roleSelected)
                } else {

                }
            }

            override fun onFailure(call: Call<UsersRes>, t: Throwable) {

            }

        })
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
        res.enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(
                call: Call<CategoryResponse>,
                response: Response<CategoryResponse>
            ) {
                if (response.isSuccessful) {
                    onDone.invoke(true, "Add succes", response.body()!!.data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }

        })
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
        res.enqueue(object : Callback<UserRes> {
            override fun onResponse(call: Call<UserRes>, response: Response<UserRes>) {
                if (response.isSuccessful) {
                    var list = mutableListOf<User>()
                    list.add(response.body()!!.data)
                    list.addAll(listUser.value)
                    listUser.value = list
                    getListUserByRole(roleSelected)
                    onDone.invoke(true, "Create Sucess", response.body()!!.data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<UserRes>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }
        })
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
        res.enqueue(object : Callback<UserRes> {
            override fun onResponse(call: Call<UserRes>, response: Response<UserRes>) {
                if (response.isSuccessful) {
                    val list = mutableListOf<User>()
                    val user = response.body()!!.data
                    list.addAll(listUser.value)
                    val index = list.indexOfFirst { it.id == id }
                    list.set(index, user)
                    listUser.value = list
                    getListUserByRole(roleSelected)
                    onDone.invoke(true, "Edit Sucess", response.body()!!.data)
                } else {
                    onDone.invoke(false, response.code().toString(), null)
                }
            }

            override fun onFailure(call: Call<UserRes>, t: Throwable) {
                onDone.invoke(false, t.message.toString(), null)
            }
        })
    }

    fun getListTableOrder() {
        val api = OrderService.createOrderApi(token)
        val res = api.getListOrdering()
        res.enqueue(object : Callback<TableOrderingList> {
            override fun onResponse(
                call: Call<TableOrderingList>,
                response: Response<TableOrderingList>
            ) {
                if (response.isSuccessful) {
                    val list = response.body()!!.data
                    listTableActive.value = list
                } else {

                }
            }

            override fun onFailure(call: Call<TableOrderingList>, t: Throwable) {
            }
        })
    }

    fun setListOrderDetailsByTable(mutableList: MutableList<OrderDetail>) {
        listOrderDetailsByTable.value = mutableList
    }

    fun completeOrder() {
        val api = OrderService.createOrderApi(token)
        val res = api.completeOrder(tableIdSubscribe)
        res.enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if (response.isSuccessful) {
                    listOrderDetailsByTable.value = mutableListOf()
                } else {

                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {

            }
        })
    }
}