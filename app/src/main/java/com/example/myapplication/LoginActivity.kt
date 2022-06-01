package com.example.myapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.ui.admin.AdminActivity
import com.example.myapplication.ui.customer.CustomerActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
    }

    private fun initListener() {
        binding.tvLogin.setOnClickListener {
            val name = binding.edtName.text.toString()
            val password = binding.editPassword.text.toString()
            if (name.isNotBlank() && password.isNotBlank()) {
                requestLogin(name, password)
            }
        }
    }

    private fun requestLogin(name: String, password: String) {
        val sharedPref = getSharedPreferences(
            getString(R.string.shared_file_name), Context.MODE_PRIVATE
        ) ?: return

        if (name == "table") {
            with(sharedPref.edit()) {
                putString(getString(R.string.key_role), "table")
                putString(getString(R.string.key_access_token), "1")
                apply()
            }
            val intent = Intent(this, CustomerActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        if (name == "admin"){
            with(sharedPref.edit()) {
                putString(getString(R.string.key_role), "admin")
                putString(getString(R.string.key_access_token), "1")
                apply()
            }
            val intent = Intent(this, AdminActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}