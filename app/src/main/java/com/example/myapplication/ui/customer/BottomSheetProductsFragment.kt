package com.example.myapplication.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.databinding.BottomSheetDialogBinding
import com.example.myapplication.ext.formatWithCurrency
import com.example.myapplication.viewmodel.CustomerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetProductsFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CustomerViewModel
    private var count = 0
    private var price = 0
    private var product: ProductEntity? = null

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
            product?.apply {
                countOrder = count
                note = binding.edtNote.text.toString()
            }
            viewModel.setListOrder()
            dismiss()
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
        product?.let {
            binding.tvPrice.text = it.price.formatWithCurrency()
            binding.tvDisciption.text = it.content
            count = it.countOrder
            price = it.price
            binding.edtCount.setText(count.toString())
            setTextAmount()
            binding.edtNote.setText(product?.note.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}