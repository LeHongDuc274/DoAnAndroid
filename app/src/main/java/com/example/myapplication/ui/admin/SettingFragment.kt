package com.example.myapplication.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProductFormBinding
import com.example.myapplication.databinding.FragmentSettingBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.TableAdminAdapter
import com.example.myapplication.viewmodel.AdminViewModel


class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private var tableAdapter = TableAdminAdapter()
    private val adminVm: AdminViewModel by lazy {
        ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListenner()
    }

    private fun initListenner() {
        adminVm.getTables()
        collectFlow(adminVm.tables){
            tableAdapter.setData(it)
        }
    }

    private fun initViews() {
        binding.rvAllTable.apply {
            adapter = tableAdapter
            layoutManager = GridLayoutManager(requireActivity(), 4)
        }
    }

}