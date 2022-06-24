package com.example.myapplication.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.core.model.CategoryEntity
import com.example.myapplication.databinding.FragmentCategoriesManageBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.CategoryAdapter
import com.example.myapplication.viewmodel.AdminViewModel
import com.example.myapplication.wiget.BaseDialogFragment

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
                    Toast.makeText(requireActivity(), "Name cann't be blank", Toast.LENGTH_LONG)
                        .show()
                }
                (viewmodel.listCategories.value.any {
                    it.name_type.equals(binding.edtAdd.text.toString())
                }) -> {
                    Toast.makeText(requireActivity(), "Name cann't be duplicate", Toast.LENGTH_LONG)
                        .show()
                }
                else -> adminViewModel.createCategory(binding.edtAdd.text.toString()) { b, str, category ->
                    if (b) {
                        val list = mutableListOf<CategoryEntity>().apply {
                            add(category!!)
                            addAll(viewmodel.listCategories.value)
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
        binding.icToolbar.tvTitle.text = "Category Manage"

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}