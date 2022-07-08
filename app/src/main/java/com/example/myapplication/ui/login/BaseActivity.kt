package com.example.myapplication.ui.login

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*


open class BaseActivity : AppCompatActivity() {
    var checkWifi = 0
    var job: Job? = null
    var wifiStateReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            val wifiStateExtra = intent.getIntExtra(
                WifiManager.EXTRA_WIFI_STATE,
                WifiManager.WIFI_STATE_UNKNOWN
            )
            when (wifiStateExtra) {
                WifiManager.WIFI_STATE_DISABLED -> {
                    checkWifi++
                }
                WifiManager.WIFI_STATE_ENABLED -> {
                    if (checkWifi != 0) {
                        job?.cancel()
                        job = CoroutineScope(Dispatchers.Default).launch {
                            delay(5000)
                            withContext(Dispatchers.Main) {
                                this@BaseActivity.recreate()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiStateReceiver)
    }
}