package com.example.myapplication.ui.customer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentOrdersBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.OrderAdapter
import com.example.myapplication.viewmodel.CustomerViewModel


class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    lateinit var viewmodel: CustomerViewModel
    private val orderAdapter = OrderAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater)
        viewmodel = ViewModelProvider(requireActivity())[CustomerViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initOnclick()
    }

    private fun initOnclick() {
        orderAdapter.setDeleteClick { p ->
            val product = viewmodel.listProducts.value.find {
                it.id == p.id
            }
            product?.apply {
                countOrder = 0
                note = "Note..."
            }
            viewmodel.setListOrder()
        }

        orderAdapter.setOnEditClick {
            val sheet = BottomSheetProductsFragment()
            sheet.arguments = bundleOf(
                "id" to it.id
            )
            sheet.show(requireActivity().supportFragmentManager, id.toString())
        }
    }

    private fun initListener() {
        viewmodel.listOrder.observe(viewLifecycleOwner) {
            orderAdapter.setData(it)
        }
        collectFlow(viewmodel.totalAmount) {
            binding.tvTotalAmount.text = it.toString() + " vnđ"
        }
    }

    private fun initView() {
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = orderAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}