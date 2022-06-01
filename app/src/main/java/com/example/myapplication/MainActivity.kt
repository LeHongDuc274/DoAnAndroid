package com.example.myapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.myapplication.ui.admin.AdminActivity
import com.example.myapplication.ui.customer.CustomerActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences(
            getString(R.string.shared_file_name), Context.MODE_PRIVATE
        ) ?: return

        val role = sharedPref.getString(getString(R.string.key_role), "")
        val accessToken = sharedPref.getString(getString(R.string.key_access_token), "")

        if (role!!.isBlank() || accessToken!!.isBlank()) {
            Log.e("tagPref", "empty")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            when (role) {
                "admin" -> {
                    val intent = Intent(this, AdminActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                "table" -> {
                    val intent = Intent(this, CustomerActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

                "staff" -> {

                }

                "kitchen" -> {

                }
                else -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }
}