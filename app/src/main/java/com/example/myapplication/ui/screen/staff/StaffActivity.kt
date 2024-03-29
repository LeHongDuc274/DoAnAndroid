package com.example.myapplication.ui.screen.staff

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.ui.screen.login.BaseActivity
import com.example.myapplication.MyApp
import com.example.myapplication.R
import com.example.myapplication.ui.model.OrderDetail
import com.example.myapplication.utils.showDialogConfirmLogout
import com.example.myapplication.databinding.ActivityStaffBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ext.gotoLogin
import com.example.myapplication.ext.showToast
import com.example.myapplication.ui.adapter.OrderDetailKitchenAdapter
import com.example.myapplication.ui.screen.kitchen.KitchenViewModel

class StaffActivity : BaseActivity() {
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
        collectFlow(kitchenVM.listMessageRequesting) {
            if (it.isEmpty()) {
                binding.tvCountMessage.visibility = View.GONE
            } else {
                binding.tvCountMessage.visibility = View.VISIBLE
                binding.tvCountMessage.text = it.size.toString()
            }
        }
        kitchenVM.getListTableMessage()
        collectFlow(kitchenVM.listProducts) {
            if (it.isNotEmpty() && !kitchenVM.connection) {
                kitchenVM.initSocket()
                kitchenVM.onShowNotice =  { mess ->
                    val builder: NotificationCompat.Builder = NotificationCompat.Builder(
                        this,
                        MyApp.CHANNEL_ID
                    )
                    builder.apply {
                        setContentTitle("Yêu cầu mới từ ${mess.user_name} ${mess.created_at}")
                        setContentText("Nội dung : ${mess.content} ")
                        setSmallIcon(R.drawable.ic_icons8_menu)
                        setPriority(NotificationCompat.PRIORITY_MAX)
                        setDefaults(NotificationCompat.DEFAULT_ALL)
                    }

                    val notificationManager = NotificationManagerCompat.from(this)
                    notificationManager.notify(1, builder.build())
                }
            }
        }

        deliveringAdapter.setOnClick {
            increaseStatus(it)
        }
        completedAdapter.setOnClick {
            increaseStatus(it)
        }

        binding.ivMessage.setOnClickListener {
            Log.e("tagXXX","message click")
            MessgeDialogFragment().show(supportFragmentManager, null)
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
          showToast(str)
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
        binding.ivMessage.setColorFilter(
            Color.parseColor("#F44336"),
            PorterDuff.Mode.SRC_IN
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        kitchenVM.finalizeSocket()
    }
}