package com.example.myapplication.ui.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
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
import com.example.myapplication.databinding.ActivityCustomerBinding
import com.example.myapplication.ext.DisplayName
import com.example.myapplication.ext.collectFlow
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
                viewmodel.initSocket {
                    binding.tvBooking.visibility = GONE
                }
            }
        }
        collectFlow(viewmodel.listProductFilter) {
            productAdapter.setListData(it)
        }
        collectFlow(viewmodel.listOrderDetails) {
            orderAdapter.setData(it.toMutableList())
        }
        collectFlow(viewmodel.totalAmount) {
            binding.tvTotalAmount.text = it.toString() + " vnđ"
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
        binding.ivIconMemu.setOnClickListener {
            val sharedPref = getSharedPreferences(
                getString(R.string.shared_file_name), Context.MODE_PRIVATE
            ) ?: return@setOnClickListener
            with(sharedPref.edit()) {
                putInt(getString(R.string.key_role), -1)
                putString(getString(R.string.key_access_token), "")
                putString(getString(R.string.key_display_name), "")
                putInt(getString(R.string.key_id), -1)
                apply()
            }
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.tvBooking.setOnClickListener {
            when {
                viewmodel.listOrderDetails.value.isEmpty() -> { // list empty
                    Toast.makeText(this, "Cart Empty", Toast.LENGTH_SHORT).show()
                }
                viewmodel.order.id == -1 && viewmodel.order.user_id == -1 -> { // order = empty -> create order
                    viewmodel.order.order_details.apply {
                        clear()
                        addAll(viewmodel.listOrderDetails.value)
                    }
                    viewmodel.createOrder { b, mess, order ->
                        Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
                        if (b) {
                            binding.tvBooking.isEnabled = false
                            binding.tvBooking.isClickable = false
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