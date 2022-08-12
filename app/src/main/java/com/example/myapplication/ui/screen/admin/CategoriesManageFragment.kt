package com.example.myapplication.ui.screen.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.ui.model.CategoryEntity
import com.example.myapplication.databinding.FragmentCategoriesManageBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.CategoryAdapter
import com.example.myapplication.utils.wiget.BaseDialogFragment

class CategoriesManageFragment() : BaseDialogFragment(R.layout.fragment_categories_manage) {

    private var _binding: FragmentCategoriesManageBinding? = null
    private val binding get() = _binding!!
    private val categoryAdapter = CategoryAdapter()
    private val viewmodel: AdminViewModel by lazy {
        ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }
    private val adminViewModel: AdminViewModel by lazy {
        ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val status = 1
        _binding = FragmentCategoriesManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListenner()
    }

    private fun initListenner() {
        binding.icToolbar.tvBack.setOnClickListener {
            dismiss()
        }
        collectFlow(viewmodel.listCategories) {
            categoryAdapter.setData(it)
        }
        binding.tvAdd.setOnClickListener {
            when {
                binding.edtAdd.text.isBlank() -> {
                    Toast.makeText(requireActivity(), "Tên không được để trống", Toast.LENGTH_LONG)
                        .show()
                }
                (viewmodel.listCategories.value.any {
                    it.name_type.equals(binding.edtAdd.text.toString())
                }) -> {
                    Toast.makeText(requireActivity(), "Tên không thể trùng nhau", Toast.LENGTH_LONG)
                        .show()
                }
                else -> adminViewModel.createCategory(binding.edtAdd.text.toString()) { b, str, category ->
                    if (b) {
                        val list = mutableListOf<CategoryEntity>().apply {
                            addAll(viewmodel.listCategories.value)
                            add(category!!)
                        }
                        viewmodel.listCategories.value = list
                    }
                    Toast.makeText(requireActivity(), str, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initViews() {
        binding.rvCategory.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = categoryAdapter
        }
        binding.icToolbar.tvTitle.text = "Quản lí phân loại"

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}