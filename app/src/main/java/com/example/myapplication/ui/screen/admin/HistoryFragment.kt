package com.example.myapplication.ui.screen.admin

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHistoryBinding
import com.example.myapplication.databinding.FragmentProductFormBinding
import com.example.myapplication.ext.clearTime
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ext.formatVN
import com.example.myapplication.ui.adapter.OrderAdapter
import com.example.myapplication.ui.adapter.OrderHistoryAdapter
import com.example.myapplication.ui.adapter.TableHistoryAdapter
import com.example.myapplication.utils.wiget.BaseDialogFragment
import java.util.*


class HistoryFragment : BaseDialogFragment(R.layout.fragment_history) {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val tableAdapter = TableHistoryAdapter()
    private val orderHistoryAdapter = OrderHistoryAdapter()
    private val orderDetailsHistoryAdapter = OrderAdapter()
    private val adminVM: AdminViewModel by lazy {
        ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
    }

    private fun initListener() {
        collectFlow(adminVM.listOrderHistory){
            tableAdapter.setData(it)
            orderHistoryAdapter.setData(it)
            binding.tvTableName.text = "Tất cả"
        }
        binding.tvTime.setOnClickListener {
            chooseStartTimeRevenue()
        }
        tableAdapter.setOnItemClick { name , it ->
            orderHistoryAdapter.setData(it.toMutableList())
            binding.tvTableName.text = name
        }
        orderHistoryAdapter.setOnViewDetail {
            binding.root.openDrawer(GravityCompat.END)
            orderDetailsHistoryAdapter.setData(it.order_details)
            binding.tvId.text = "ID đơn hàng : ${it.id}"
        }
        adminVM.getHistory()
    }

    private fun initViews() {
        binding.rvTableHistory.apply {
            layoutManager = GridLayoutManager(requireActivity(), 4)
            adapter = tableAdapter
        }
        binding.rvOrderHistory.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = orderHistoryAdapter
        }
        binding.rvOrderDetailsHistory.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = orderDetailsHistoryAdapter
        }
        binding.tvTime.text = Calendar.getInstance().apply { clearTime() }.formatVN()
    }
    private fun chooseStartTimeRevenue() {
        val calendar = adminVM.startTimeRevenue
        val datetimeDialog =
            DatePickerDialog(requireActivity(), object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) = Unit
            }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DATE])
        datetimeDialog.datePicker.maxDate = Calendar.getInstance().apply {
            clearTime()
        }.timeInMillis
        datetimeDialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            "Đồng ý",
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    val picked = datetimeDialog.datePicker
                    calendar.set(picked.year, picked.month, picked.dayOfMonth)
                    calendar.clearTime()
                    binding.tvTime.text = calendar.formatVN()
                    adminVM.getHistory(calendar.formatVN())
                }
            })
        datetimeDialog.show()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}