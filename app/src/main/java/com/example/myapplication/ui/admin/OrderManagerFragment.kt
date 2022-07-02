package com.example.myapplication.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.core.ItemStatus
import com.example.myapplication.databinding.FragmentOrderManagerBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.OrderAdapter
import com.example.myapplication.ui.adapter.TableOrderAdapter
import com.example.myapplication.viewmodel.AdminViewModel


class OrderManagerFragment : Fragment() {

    private var _binding: FragmentOrderManagerBinding? = null
    private val binding get() = _binding!!
    private val tableAdapter = TableOrderAdapter()
    private val orderDetailAdapter = OrderAdapter()
    private val adminViewModel: AdminViewModel by lazy {
        ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOrderManagerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
    }

    private fun initListener() {
        collectFlow(adminViewModel.listUser) {
            if (it.isNotEmpty()) adminViewModel.getListTableOrder()
        }
        collectFlow(adminViewModel.listTableActive) {
            if (it.isNotEmpty()) {
                tableAdapter.setData(it)
                adminViewModel.getListTableMessage()
            }
        }
        collectFlow(adminViewModel.listMessageRequesting) {
            tableAdapter.setListMessage(it)
        }
        collectFlow(adminViewModel.listOrderDetailsByTable) {
            orderDetailAdapter.setData(it)
        }
        tableAdapter.setOnItemClick {
            binding.tvDetailTable.text = "Đơn hàng của " + it.display_name
            adminViewModel.subscribeChannel(it.user_id)
            adminViewModel.getCurrentOrder(it.user_id) { b, mess, res ->
                if (b && res != null) adminViewModel.setListOrderDetailsByTable(
                    res.data?.order_details ?: mutableListOf()
                )
            }
        }
        tableAdapter.setOnMessageClick {
            val sheet = BottomSheetMessagesFragment(requireActivity())
            sheet.arguments = bundleOf(
                "id" to it.user_id
            )
            sheet.show(childFragmentManager, id.toString())
        }
        binding.tvComplete.setOnClickListener {
            // Tạm thời để DELIVERING , làm màn staff xong chuyen lai thanh DELEVERED
            if (adminViewModel.listOrderDetailsByTable.value.isEmpty() || adminViewModel.listOrderDetailsByTable.value.any { it.status < ItemStatus.DELIVERING.status }) {
                Toast.makeText(requireActivity(), "Đơn hàng chưa hoàn thành", Toast.LENGTH_SHORT).show()
            } else {
                adminViewModel.completeOrder()
            }
        }
    }

    private fun initView() {
        binding.rvTable.apply {
            layoutManager = GridLayoutManager(requireActivity(), 3)
            adapter = tableAdapter
        }
        binding.rvDetailTable.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = orderDetailAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}