package com.example.myapplication.ui.screen.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.screen.admin.AdminActivity
import com.example.myapplication.ui.screen.customer.CustomerActivity
import com.example.myapplication.ui.screen.kitchen.KitchenActivity
import com.example.myapplication.ui.screen.staff.StaffActivity
import com.example.myapplication.ui.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginVm: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
    }

    private fun initListener() {
        collectFlow(loginVm.isLoading){
            if (it){
                binding.loading.visibility = View.VISIBLE
                binding.loading.isClickable = true
            } else {
                binding.loading.visibility = View.GONE
                binding.loading.isClickable = false
            }
        }
        binding.tvLogin.setOnClickListener {
            val name = binding.edtName.text.toString()
            val password = binding.editPassword.text.toString()
            if (name.isNotBlank() && password.isNotBlank()) {
                requestLogin(name, password)
            }
        }
    }

    private fun requestLogin(login_id: String, password: String) {
        val sharedPref = getSharedPreferences(
            getString(R.string.shared_file_name), Context.MODE_PRIVATE
        ) ?: return
        loginVm.login(login_id, password) {
            if (it == null) {
                Toast.makeText(this, "name or password fail", Toast.LENGTH_LONG).show()
            } else {
                with(sharedPref.edit()) {
                    putInt(getString(R.string.key_role), it.role)
                    putString(getString(R.string.key_access_token), it.access_token)
                    putString(getString(R.string.key_display_name),it.display_name)
                    putInt(getString(R.string.key_id),it.id)
                    apply()
                }
                when (it.role) {
                    0 -> { // admin
                        val intent = Intent(this, AdminActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    1 -> { // staff
                        val intent = Intent(this, StaffActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    2 -> { // kitchen
                        val intent = Intent(this, KitchenActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    3 -> { // table
                        val intent = Intent(this, CustomerActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            }
        }


//        if (login_id == "table") {
//            with(sharedPref.edit()) {
//                putString(getString(R.string.key_role), "table")
//                putString(getString(R.string.key_access_token), "1")
//                apply()
//            }
//            val intent = Intent(this, CustomerActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//        }
//
//        if (login_id == "admin") {
//            with(sharedPref.edit()) {
//                putString(getString(R.string.key_role), "admin")
//                putString(getString(R.string.key_access_token), "1")
//                apply()
//            }
//        }
    }
}