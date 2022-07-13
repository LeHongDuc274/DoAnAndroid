package com.example.myapplication.ui.customer

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.ui.login.BaseActivity
import com.example.myapplication.core.utils.showDialogConfirmLogout
import com.example.myapplication.core.utils.showDialogResquestStaff
import com.example.myapplication.databinding.ActivityCustomerBinding
import com.example.myapplication.ext.DisplayName
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ext.gotoLogin
import com.example.myapplication.ext.showToast
import com.example.myapplication.ui.adapter.CategoryTabAdapter
import com.example.myapplication.ui.adapter.OrderAdapter
import com.example.myapplication.ui.adapter.ProductManagerAdapter
import com.example.myapplication.ui.adapter.ProductsAdapter
import com.example.myapplication.viewmodel.CustomerViewModel


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
            if (it.isNotEmpty() && !viewmodel.connection) {
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
            productAdapter.setCategory(it)
        }
        collectFlow(viewmodel.arletMessage) {
            if (it.isNotBlank()) {
                AlertDialog.Builder(this)
                    .setTitle("Đơn hàng bạn gặp sự cố")
                    .setMessage("Sản phẩm $it đã được xoá khỏi đơn hàng")
                    .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which -> })
                    .setNegativeButton("", null).show()
            }
        }
    }

    private fun initOnClick() {
        productAdapter.setOnClick {
            if (it.status == 0) showBottomSheetDialog(it.id) else showToast("Món ăn này đã hết")
        }
        categoryAdapter.setOnClickItem {
            viewmodel.setListProductByCategory(it.id)
        }
        orderAdapter.setDeleteClick { p ->
            if (p.id == -1) {
                viewmodel.listOrderDetails.value =
                    (viewmodel.listOrderDetails.value - p).toMutableList()
            } else {
                viewmodel.deleteOrderDetails(p.id)
            }
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
                    Toast.makeText(this, "Mật khẩu trống", Toast.LENGTH_SHORT).show()
                }
            }
            false

        }

        binding.tvBooking.setOnClickListener {
            when {
                viewmodel.listOrderDetails.value.isEmpty() -> { // list empty
                    showToast("Đơn hàng không thể trống")
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

        binding.ivCart.setOnClickListener {
            binding.root.openDrawer(GravityCompat.END)
        }
        binding.ivRequestStaff.setOnClickListener {
            showDialogResquestStaff { content ->
                viewmodel.createMessageRequest(content) {
                    showToast(it)
                }
            }
        }
    }


    private fun initView() {
       val gridManager = GridLayoutManager(this@CustomerActivity, 4) .apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (productAdapter.getItemViewType(position) == ProductsAdapter.TYPE_CATEGORY) 4 else 1
                }
            }
        }
        binding.rvListProduct.apply {
            layoutManager = gridManager
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

        binding.tvTableName.text = application.DisplayName()
        binding.ivRequestStaff.setColorFilter(
            Color.parseColor("#FFFFFF"),
            PorterDuff.Mode.SRC_IN
        )
        binding.ivCart.setColorFilter(
            Color.parseColor("#FFFFFF"),
            PorterDuff.Mode.SRC_IN
        )
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
        viewmodel.arletMessage.value = ""
    }
}