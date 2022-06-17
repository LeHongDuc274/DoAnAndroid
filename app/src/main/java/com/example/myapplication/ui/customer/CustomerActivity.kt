package com.example.myapplication.ui.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCustomerBinding
import com.example.myapplication.ext.DisplayName
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.CategoryTabAdapter
import com.example.myapplication.ui.adapter.OrderAdapter
import com.example.myapplication.ui.adapter.ProductsAdapter
import com.example.myapplication.viewmodel.CustomerViewModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vinted.actioncable.client.kotlin.ActionCable
import com.vinted.actioncable.client.kotlin.Channel
import com.vinted.actioncable.client.kotlin.Consumer
import okhttp3.*
import okio.ByteString
import java.math.BigInteger
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*


class CustomerActivity : AppCompatActivity() {

    lateinit var client: OkHttpClient
    lateinit var ws: WebSocket
    lateinit var consumer: Consumer
    private lateinit var binding: ActivityCustomerBinding
    private val productAdapter = ProductsAdapter()
    private var categoryAdapter = CategoryTabAdapter(this)
    private val orderAdapter = OrderAdapter()
    private val viewmodel: CustomerViewModel by lazy {
        ViewModelProvider(this).get(CustomerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initListener()
        initOnClick()
    }

    override fun onResume() {
        super.onResume()
        //initWebSocket()
        // start()
    }

    private fun initListener() {
        collectFlow(viewmodel.listProducts) {
            viewmodel.setListProductByCategory()
        }
        collectFlow(viewmodel.listProductFilter) {
            productAdapter.setListData(it)
        }
        collectFlow(viewmodel.listOrderDetails) {
            orderAdapter.setData(it.toMutableList())
        }
        collectFlow(viewmodel.totalAmount) {
            binding.tvTotalAmount.text = it.toString() + " vnđ"
        }
        collectFlow(viewmodel.listCategories) {
            categoryAdapter.setData(it)
        }
    }

    private fun initOnClick() {
        productAdapter.setOnClick {
            showBottomSheetDialog(it.id)
        }
        categoryAdapter.setOnClickItem {
            viewmodel.setListProductByCategory(it.id)
        }
        orderAdapter.setDeleteClick { p ->
            val orderDetail = viewmodel.listOrderDetails.value.find {
                it.id == p.id
            }
            orderDetail?.apply {
                amount = 0
                note = "Note..."
            }
            // call API delete this order Detail
            // viewmodel.setListOrder(list)
        }

        orderAdapter.setOnEditClick {
            val sheet = BottomSheetProductsFragment(this)
            sheet.arguments = bundleOf(
                "id" to it.product_id
            )
            sheet.show(this.supportFragmentManager, it.product_id.toString())
        }
        // test LogOut
        binding.ivIconMemu.setOnClickListener {
            val sharedPref = getSharedPreferences(
                getString(R.string.shared_file_name), Context.MODE_PRIVATE
            ) ?: return@setOnClickListener
            with(sharedPref.edit()) {
                putInt(getString(R.string.key_role), -1)
                putString(getString(R.string.key_access_token), "")
                putString(getString(R.string.key_display_name),"")
                apply()
            }
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.tvBooking.setOnClickListener {
            when {
                viewmodel.listOrderDetails.value.isEmpty() -> { // list empty
                    Toast.makeText(this, "Cart Empty", Toast.LENGTH_SHORT).show()
                }
                viewmodel.order.id == -1 && viewmodel.order.user_id == -1 -> { // order = empty -> create order
                    viewmodel.order.order_details.apply {
                        clear()
                        addAll(viewmodel.listOrderDetails.value)
                    }
                    viewmodel.createOrder { b, mess, order ->
                        Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
                        if (b) {
                            binding.tvBooking.isEnabled = false
                            binding.tvBooking.isClickable = false
                            binding.tvBooking.visibility = GONE
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.rvListProduct.apply {
            layoutManager = GridLayoutManager(this@CustomerActivity, 4)
            adapter = productAdapter
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@CustomerActivity)
            adapter = orderAdapter
        }

        binding.rvCategories.apply {
            layoutManager =
                LinearLayoutManager(this@CustomerActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        binding.tvTime.text = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("GMT+7")).time)
        binding.tvTableName.text = application.DisplayName()
    }

    private fun showBottomSheetDialog(id: Int) {
        val sheet = BottomSheetProductsFragment(this)
        sheet.arguments = bundleOf(
            "id" to id
        )
        sheet.show(supportFragmentManager, id.toString())
    }

    override fun onPause() {
        super.onPause()
        finalizeSocket()
        //  consumer.disconnect()
    }

    private fun finalizeSocket() {

    }

    private fun initWebSocket() {
        client = OkHttpClient()

        try {
            // val request = Request.Builder().url("ws://192.168.1.5:3000/api/v1/cable").build()
            val uri = URI("ws://192.168.1.5:3000/cable")
            consumer = ActionCable.createConsumer(uri)
//            val listenner = SocketListener()
//            ws = client.newWebSocket(request,listenner)
//            client.dispatcher().executorService().shutdown()
            val chatChannel = Channel("ChatsChannel")
            val subscription = consumer.subscriptions.create(chatChannel)
            Log.e("tagcc", "ccc")
            subscription.onConnected = {
                // Called when the subscription has been successfully completed
                Log.e("tagggg", "start")
            }

            subscription.onRejected = {
                // Called when the subscription is rejected by the server
                Log.e("tagSocket", "onRejected")
            }

            subscription.onReceived = { data: Any? ->
                Log.e("tagSocket", "onReceived")
                // Called when the subscription receives data from the server
                // Possible types...
                when (data) {
                    is Int -> {
                    }
                    is Long -> {
                    }
                    is BigInteger -> {
                    }
                    is String -> {
                    }
                    is Double -> {
                    }
                    is Boolean -> {
                    }
                    is JsonObject -> {
                    }
                    is JsonArray -> {
                    }
                }
            }

            subscription.onDisconnected = {
                // Called when the subscription has been closed
            }

            subscription.onFailed = { error ->
                // Called when the subscription encounters any error
                Log.e("tagSocket", "failed")
            }
            consumer.connect()

        } catch (e: Exception) {
            Log.e("tagggg", "e")
        }
    }


    private fun start() {
        client = OkHttpClient()
        val request: Request = Request.Builder().url("wss://192.168.1.5:3000/cable").build()
        val listener = EchoWebSocketListener()
        val ws = client.newWebSocket(request, listener)
        client.dispatcher().executorService().shutdown()
        client = OkHttpClient()

    }

    inner class EchoWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send("{\"command\":\"subscribe\", \"identifier\":\"{\\\"channel\\\":\\\"ChatsChannel\\\"}\"}")
            Log.e("tagxx", "onOpen")
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.e("tagxx", "onMessage")
        }


        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("tagxx", "onFail")

        }
    }

    override fun onBackPressed() {
        if (binding.root.isDrawerOpen(GravityCompat.END)) {
            binding.root.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}