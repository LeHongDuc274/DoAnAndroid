package com.example.myapplication.ui.customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCustomerBinding
import com.example.myapplication.viewmodel.CustomerViewModel

class CustomerActivity : AppCompatActivity() {

    lateinit var viewmodel : CustomerViewModel

    private lateinit var binding: ActivityCustomerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewmodel = ViewModelProvider(this).get(CustomerViewModel::class.java)
        initView()
    }

    private fun initView() {
        val fm = supportFragmentManager
        fm.beginTransaction().add(binding.fcvMenu.id,ProductsFragment()).commit()
        fm.beginTransaction().add(binding.fcvOrder.id,OrdersFragment()).commit()
    }
}