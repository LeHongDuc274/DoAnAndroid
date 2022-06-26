package com.example.myapplication.ui.staff

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.utils.showDialogConfirmLogout
import com.example.myapplication.databinding.ActivityKitchenBinding
import com.example.myapplication.databinding.ActivityStaffBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ext.gotoLogin
import com.example.myapplication.ext.showToast
import com.example.myapplication.ui.adapter.OrderDetailKitchenAdapter
import com.example.myapplication.viewmodel.KitchenViewModel

class StaffActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaffBinding
    private val deliveringAdapter = OrderDetailKitchenAdapter()
    private val completedAdapter = OrderDetailKitchenAdapter()
    private val kitchenVM: KitchenViewModel by lazy {
        ViewModelProvider(this)[KitchenViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initListener()
    }

    private fun initListener() {
        kitchenVM.initViewModel()
        collectFlow(kitchenVM.listProducts) {
            if (it.isNotEmpty()) {
                kitchenVM.initSocket()
            }
        }
        deliveringAdapter.setOnClick {
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
                    showToast("Paswword is Blank")
                }
            }
        }


        collectFlow(kitchenVM.listDelivering) {
            deliveringAdapter.setData(it)
        }
        collectFlow(kitchenVM.listComplete) {
            completedAdapter.setData(it)
        }
    }

    private fun increaseStatus(orderDetail: OrderDetail) {
        orderDetail.apply {
            status = status.inc()
        }
        kitchenVM.updateOrderDetails(orderDetail) { b, str, details ->
            Toast.makeText(this, str, Toast.LENGTH_LONG).show()
        }
    }


    private fun initViews() {
        binding.rvCompleted.apply {
            layoutManager = LinearLayoutManager(this@StaffActivity)
            adapter = completedAdapter
        }
        binding.rvDelivering.apply {
            layoutManager = LinearLayoutManager(this@StaffActivity)
            adapter = deliveringAdapter
        }
//        binding.rvNotice.apply {
//            layoutManager = LinearLayoutManager(this@StaffActivity)
//            adapter = completedAdapter
//        }
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