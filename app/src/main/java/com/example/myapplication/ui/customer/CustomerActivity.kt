package com.example.myapplication.ui.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.BaseActivity
import com.example.myapplication.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.core.utils.showDialogConfirmLogout
import com.example.myapplication.databinding.ActivityCustomerBinding
import com.example.myapplication.ext.DisplayName
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ext.gotoLogin
import com.example.myapplication.ext.showToast
import com.example.myapplication.ui.adapter.CategoryTabAdapter
import com.example.myapplication.ui.adapter.OrderAdapter
import com.example.myapplication.ui.adapter.ProductsAdapter
import com.example.myapplication.viewmodel.CustomerViewModel
import java.text.SimpleDateFormat
import java.util.*


class CustomerActivity : BaseActivity() {
    private lateinit var binding: ActivityCustomerBinding
    private val productAdapter = ProductsAdapter()
    private var categoryAdapter = CategoryTabAdapter(this)
    private val orderAdapter = OrderAdapter()
    private val viewmodel: CustomerViewModel by lazy {
        ViewModelProvider(this).get(CustomerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initListener()
        initOnClick()
    }

    private fun initListener() {
        viewmodel.initViewModel()
        collectFlow(viewmodel.listProducts) {
            viewmodel.setListProductByCategory()
            if (it.isNotEmpty()) {
                viewmodel.initSocket { orderGone ->
                    binding.tvBooking.visibility = if (orderGone) GONE else VISIBLE
                    binding.tvBooking.isClickable = true
                }
            }
        }
        collectFlow(viewmodel.listProductFilter) {
            productAdapter.setListData(it)
        }
        collectFlow(viewmodel.listOrderDetails) {
            orderAdapter.setData(it.toMutableList())
            binding.tvTotalAmount.text = " ${viewmodel.subTotal()} vnđ"
        }

        collectFlow(viewmodel.listCategories) {
            categoryAdapter.setData(it)
        }
    }

    private fun initOnClick() {
        productAdapter.setOnClick {
            showBottomSheetDialog(it.id)
        }
        categoryAdapter.setOnClickItem {
            viewmodel.setListProductByCategory(it.id)
        }
        orderAdapter.setDeleteClick { p ->
            val orderDetail = viewmodel.listOrderDetails.value.find {
                it.id == p.id
            }
            orderDetail?.apply {
                amount = 0
                note = "Note..."
            }
            // call API delete this order Detail
            // viewmodel.setListOrder(list)
        }

        orderAdapter.setOnEditClick {
            val sheet = BottomSheetProductsFragment(this)
            sheet.arguments = bundleOf(
                "id" to it.product_id
            )
            sheet.show(this.supportFragmentManager, it.product_id.toString())
        }
        // test LogOut
        binding.ivIconMemu.setOnLongClickListener {
            showDialogConfirmLogout {
                if (it.isNotBlank()) {
                    viewmodel.logout(it) { b, str ->
                        if (b) {
                            gotoLogin()
                        } else {
                            Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is blank", Toast.LENGTH_SHORT).show()
                }
            }
            false

        }

        binding.tvBooking.setOnClickListener {
            when {
                viewmodel.listOrderDetails.value.isEmpty() -> { // list empty
                    showToast("Cart Empty")
                }
                viewmodel.order.id == -1 && viewmodel.order.user_id == -1 -> { // order = empty -> create order
                    viewmodel.order.order_details.apply {
                        clear()
                        addAll(viewmodel.listOrderDetails.value)
                    }
                    viewmodel.createOrder { b, mess, order ->
                        Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
                        if (b) {
                            binding.tvBooking.visibility = GONE
                        }
                    }
                }
            }
        }
    }


    private fun initView() {
        binding.rvListProduct.apply {
            layoutManager = GridLayoutManager(this@CustomerActivity, 4)
            adapter = productAdapter
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@CustomerActivity)
            adapter = orderAdapter
        }

        binding.rvCategories.apply {
            layoutManager =
                LinearLayoutManager(this@CustomerActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        binding.tvTime.text = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("GMT+7")).time)
        binding.tvTableName.text = application.DisplayName()
    }

    private fun showBottomSheetDialog(id: Int) {
        val sheet = BottomSheetProductsFragment(this)
        sheet.arguments = bundleOf(
            "id" to id
        )
        sheet.show(supportFragmentManager, id.toString())
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
        viewmodel.finalizeSocket()
    }
}