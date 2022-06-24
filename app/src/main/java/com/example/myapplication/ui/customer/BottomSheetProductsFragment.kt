package com.example.myapplication.ui.customer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.myapplication.core.ItemStatus
import com.example.myapplication.core.model.OrderDetail
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.databinding.BottomSheetDialogBinding
import com.example.myapplication.ext.formatWithCurrency
import com.example.myapplication.ui.admin.ProductFormFragment.Companion.EDIT_MODE
import com.example.myapplication.ui.admin.ProductFormFragment.Companion.NEW_MODE
import com.example.myapplication.viewmodel.CustomerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BottomSheetProductsFragment(private val activity: Context) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CustomerViewModel
    private var count = 0
    private var price = 0
    private var product: ProductEntity? = null
    private var mode = NEW_MODE
    private var orderDetails: OrderDetail? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetDialogBinding.inflate(inflater)
        viewModel = ViewModelProvider(requireActivity())[CustomerViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initLitener()
    }

    private fun initLitener() {
        binding.ivSub.setOnClickListener {
            if (count > 0) {
                count--
                setTextAmount()
            }
        }
        binding.ivPlus.setOnClickListener {
            count++
            setTextAmount()
        }
        binding.tvAddToCart.setOnClickListener {
            if (mode == NEW_MODE) {
                submitNewMode()
            } else {
                //Edit Mode
                submitEditMode()
            }
            dismiss()
        }
    }

    private fun submitEditMode() {
        val detail = orderDetails!!.copyInstance()
        detail.amount = count
        detail.note = binding.edtNote.text.toString()
        GlobalScope.launch {
            val list = mutableListOf<OrderDetail>()
            list.addAll(viewModel.listOrderDetails.value)
            val index = viewModel.listOrderDetails.value.indexOfFirst {
                it.product_id == orderDetails!!.product_id && it.status < 2
            }
            if ( viewModel.order.id != -1) { // đã submit , đã có Order
                if (detail.status < ItemStatus.PREPARING.status) { // preparing
                    viewModel.updateOrderDetails(detail) { b, mess, data ->
                        Toast.makeText(activity, mess, Toast.LENGTH_LONG).show()
                        if (b) {
                            list.set(index, detail)
                            viewModel.setListOrder(list)
                        }
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "this product was preparing",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else { // chưa có Order
                list.set(index, detail)
                viewModel.setListOrder(list)
            }
        }
    }

    private fun submitNewMode() {
        val detail = OrderDetail(
            product_id = product!!.id,
            amount = count,
            note = binding.edtNote.text.toString(),
            user_id = viewModel.order.user_id,
            order_id = viewModel.order.id
        )
        val list = mutableListOf<OrderDetail>()
        list.addAll(viewModel.listOrderDetails.value)

        if (viewModel.order.id != -1) {
            viewModel.createOrderDetail(detail) { b, mess, data ->
                Toast.makeText(activity, mess, Toast.LENGTH_LONG).show()
                if (b) {
                    list.add(data!!)
                    viewModel.listOrderDetails.value = list
                }
            }
        } else {
            list.add(detail)
            viewModel.listOrderDetails.value = list
        }
    }

    private fun setTextAmount() {
        val priceStr = (price * count).formatWithCurrency()
        val textPrice = String.format("Add to cart (%s)", priceStr)
        binding.tvAddToCart.setText(textPrice)
        binding.edtCount.setText(count.toString())
    }

    private fun initView() {
        val bundle = arguments
        val id = bundle?.getInt("id", 0) ?: 0

        product = viewModel.listProducts.value.find {
            it.id == id
        }
        orderDetails = viewModel.listOrderDetails.value.find {
            (it.product_id == id && it.status < ItemStatus.PREPARING.status) // check ỏdetails có tồn tại và chưa preparing k
        }
        if (orderDetails == null) { // tạo orderDetails mới
            mode = NEW_MODE
        } else { // Thay đổi orderDetails đã có
            mode = EDIT_MODE
            orderDetails.let {
                count = it!!.amount
                binding.edtCount.setText(count.toString())
                binding.edtNote.setText(it.note)
            }
        }
        product?.let {
            binding.tvPrice.text = it.price.formatWithCurrency()
            binding.tvDisciption.text = it.content
            price = it.price
            val url = it.image_url
            Glide.with(binding.ivProduct.context).load(url).circleCrop().into(binding.ivProduct)
        }
        setTextAmount()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}