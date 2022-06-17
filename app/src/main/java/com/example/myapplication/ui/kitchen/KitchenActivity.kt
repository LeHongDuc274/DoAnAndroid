package com.example.myapplication.ui.kitchen

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.ActivityKitchenBinding
import com.example.myapplication.ext.AccessToken
import com.example.myapplication.ext.collectFlow
import okhttp3.*
import okio.ByteString
import org.json.JSONObject


class KitchenActivity : AppCompatActivity() {

    lateinit var client: OkHttpClient
    lateinit var ws: WebSocket
    lateinit var request: Request
    private lateinit var binding: ActivityKitchenBinding
    private val kitchenVM: KitchenViewModel by lazy {
        ViewModelProvider(this)[KitchenViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKitchenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
//        initWebSocket()
        initSocket()
    }

    override fun onDestroy() {
        super.onDestroy()
        finalizeSocket()
    }

    private fun initViews() {
        collectFlow(kitchenVM.listPending) {
            Log.e("tagKitchen", it.size.toString())
        }
    }

    fun initSocket() {
        client = OkHttpClient()
        // below url is public
        request = Request.Builder().url("ws://192.168.1.2:3000/cable")
            .addHeader("token", application.AccessToken()).build()
        Log.e("tagToken",application.AccessToken())
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.e("TagStart","Start Connect")
                val paramObject = JSONObject()
                paramObject.put("command", "subscribe")
                paramObject.put("identifier", "{\"channel\":\"MessageChannel\"}")
                ws.send(paramObject.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("tagMessage",text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//                super.onMessage(webSocket, bytes)
                Log.e("tagMessageByte",bytes.toString())
            }

        })
        client.dispatcher().executorService().shutdown()
    }

    fun finalizeSocket() {
        ws.send("finishing")
        ws.close(101, null)
        client.connectionPool().evictAll()
    }
}