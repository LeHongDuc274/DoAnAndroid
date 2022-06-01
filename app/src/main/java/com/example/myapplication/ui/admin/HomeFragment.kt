package com.example.myapplication.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.CategoryTabAdapter
import com.example.myapplication.ui.adapter.ProductManagerAdapter
import com.example.myapplication.viewmodel.CustomerViewModel


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val prAdapter = ProductManagerAdapter()
    private lateinit var cateAdapter: CategoryTabAdapter
    private lateinit var cmViewModel: CustomerViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        cmViewModel = ViewModelProvider(requireActivity())[CustomerViewModel::class.java]
        cateAdapter = CategoryTabAdapter(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
    }

    private fun initListener() {
        collectFlow(cmViewModel.listProducts) {
            prAdapter.setData(it)
        }
        collectFlow(cmViewModel.listCategories) {
            cateAdapter.setData(it)
        }

        cateAdapter.setOnClickItem {

        }

        binding.tvManagerCategories.setOnClickListener {
            val fm = childFragmentManager
            CategoriesManageFragment().show(fm, null)
        }
    }

    private fun initViews() {
        binding.rvManagerProduct.apply {
            layoutManager = GridLayoutManager(requireActivity(), 4)
            adapter = prAdapter
        }
        binding.rvCategories.apply {
            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            adapter = cateAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}