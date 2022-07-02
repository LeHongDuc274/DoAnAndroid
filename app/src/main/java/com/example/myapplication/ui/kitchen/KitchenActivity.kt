package com.example.myapplication.ui.kitchen

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.BaseActivity
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.utils.showDialogConfirmLogout
import com.example.myapplication.databinding.ActivityKitchenBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ext.gotoLogin
import com.example.myapplication.ext.showToast
import com.example.myapplication.ui.adapter.OrderDetailKitchenAdapter
import com.example.myapplication.viewmodel.KitchenViewModel


class KitchenActivity : BaseActivity() {

    private lateinit var binding: ActivityKitchenBinding
    private val pendingAdapter = OrderDetailKitchenAdapter()
    private val preparingAdapter = OrderDetailKitchenAdapter()
    private val completedAdapter = OrderDetailKitchenAdapter()

    private val kitchenVM: KitchenViewModel by lazy {
        ViewModelProvider(this)[KitchenViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKitchenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initListener()
    }

    private fun initListener() {
        kitchenVM.initViewModel()
        collectFlow(kitchenVM.listProducts) {
            if (it.isNotEmpty() && !kitchenVM.connection) {
                kitchenVM.initSocket()
            }
        }
        pendingAdapter.setOnClick {
            increaseStatus(it)
        }
        preparingAdapter.setOnClick {
            increaseStatus(it)
        }
        completedAdapter.setOnClick {
            increaseStatus(it)
        }

        binding.ivLogout.setOnClickListener {
            showDialogConfirmLogout {
                if (it.isNotBlank()) {
                    kitchenVM.logout(it) { b, mess ->
                        if (b) {
                            gotoLogin()
                        } else {
                            showToast(mess)
                        }
                    }
                } else {
                    showToast("Mật khẩu trống")
                }
            }
        }

        collectFlow(kitchenVM.listPending) {
            pendingAdapter.setData(it)
        }
        collectFlow(kitchenVM.listPreparing) {
            preparingAdapter.setData(it)
        }
        collectFlow(kitchenVM.listComplete) {
            completedAdapter.setData(it)
        }
        collectFlow(kitchenVM.isLoading){
            showLoading(it)
        }
    }

    private fun showLoading(b: Boolean){
        if(b){
            binding.llLoading.visibility = View.VISIBLE
            binding.llLoading.isClickable = true
        } else {
            binding.llLoading.visibility = View.INVISIBLE
            binding.llLoading.isClickable = false
        }
    }
    private fun increaseStatus(orderDetail: OrderDetail) {
        kitchenVM.isLoading.value = true
        orderDetail.apply {
            status = status.inc()
        }
        kitchenVM.updateOrderDetails(orderDetail) { b, str, details ->
            kitchenVM.isLoading.value = false
            Toast.makeText(this, str, Toast.LENGTH_LONG).show()
        }
    }


    private fun initViews() {
        binding.rvPending.apply {
            layoutManager = LinearLayoutManager(this@KitchenActivity)
            adapter = pendingAdapter
        }
        binding.rvPreparing.apply {
            layoutManager = LinearLayoutManager(this@KitchenActivity)
            adapter = preparingAdapter
        }
        binding.rvCompleted.apply {
            layoutManager = LinearLayoutManager(this@KitchenActivity)
            adapter = completedAdapter
        }
        binding.ivLogout.setColorFilter(
            Color.parseColor("#F44336"),
            PorterDuff.Mode.SRC_IN
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        kitchenVM.finalizeSocket()
    }
}