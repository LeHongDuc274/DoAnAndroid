package com.example.myapplication.ui.kitchen

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.ui.login.BaseActivity
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.utils.showDialogConfirmLogout
import com.example.myapplication.databinding.ActivityKitchenBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ext.gotoLogin
import com.example.myapplication.ext.showToast
import com.example.myapplication.ui.adapter.OrderDetailKitchenAdapter
import com.example.myapplication.ui.adapter.ProductManagerAdapter
import com.example.myapplication.viewmodel.KitchenViewModel


class KitchenActivity : BaseActivity() {

    private lateinit var binding: ActivityKitchenBinding
    private val pendingAdapter = OrderDetailKitchenAdapter(false)
    private val preparingAdapter = OrderDetailKitchenAdapter()
    private val completedAdapter = OrderDetailKitchenAdapter()
    private val productAdapetr = ProductManagerAdapter()
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
            productAdapetr.setRawData(it)
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
        collectFlow(kitchenVM.isLoading) {
            showLoading(it)
        }
        pendingAdapter.setOnClick {
            increaseStatus(it)
        }
        pendingAdapter.setOnDeleteCick { productID ->
            AlertDialog.Builder(this)
                .setTitle("X??c nh???n")
                .setMessage("L??u ??: C??c ????n h??ng ??ang ch??? c???a s???n ph???m n??y c??ng s??? b??? xo??")
                .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                    changeStatusProduct(productID)
                })
                .setNegativeButton("Hu???", null).show()
        }
        preparingAdapter.setOnClick {
            increaseStatus(it)
        }
        completedAdapter.setOnClick {
            increaseStatus(it)
        }
        productAdapetr.onClick {
            val mesage =
                if (it.status == 0) "L??u ??: C??c ????n h??ng ??ang ch??? c???a s???n ph???m n??y c??ng s??? b??? xo??" else "M??? l???i s???n ph???m n??y!"
            AlertDialog.Builder(this)
                .setTitle("X??c nh???n")
                .setMessage(mesage)
                .setPositiveButton("Ok") { _, _ ->
                    changeStatusProduct(it.id)
                }
                .setNegativeButton("Hu???", null).show()
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
                    showToast("M???t kh???u tr???ng")
                }
            }
        }
        binding.ivSetting.setOnClickListener {
            binding.root.openDrawer(GravityCompat.END)
        }
    }

    private fun changeStatusProduct(productID: Int) {
        kitchenVM.isLoading.value = true
        kitchenVM.changeStatusProduct(productID) { b, mess, data ->
            kitchenVM.isLoading.value = false
        }
    }

    private fun showLoading(b: Boolean) {
        if (b) {
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
        binding.rvProduct.apply {
            layoutManager = GridLayoutManager(this@KitchenActivity, 5)
            adapter = productAdapetr
        }
    }

    override fun onBackPressed() {
        if (binding.root.isDrawerOpen(GravityCompat.END)) {
            binding.root.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kitchenVM.finalizeSocket()
    }
}