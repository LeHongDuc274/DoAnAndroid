package com.example.myapplication.ui.screen.admin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.core.PRODUCT_EXTRA_KEY
import com.example.myapplication.ui.model.ProductEntity
import com.example.myapplication.utils.GsonUtils
import com.example.myapplication.databinding.FragmentProductManagerBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.CategoryTabAdapter
import com.example.myapplication.ui.adapter.ProductManagerAdapter


class ProductManagerFragment : Fragment() {
    private var _binding: FragmentProductManagerBinding? = null
    private val binding get() = _binding!!
    private val prAdapter = ProductManagerAdapter()
    private lateinit var cateAdapter: CategoryTabAdapter
    private lateinit var viewModel: AdminViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductManagerBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[AdminViewModel::class.java]
        cateAdapter = CategoryTabAdapter(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
    }

    private fun initListener() {
        collectFlow(viewModel.listProducts) {
            viewModel.setListProductByCategory()
        }
        collectFlow(viewModel.listCategories) {
//            Log.e("lítTAg",it.toString())
            cateAdapter.setData(it)
        }
        collectFlow(viewModel.listProductFilter) {
            prAdapter.setData(it)
        }

        cateAdapter.setOnClickItem {
            viewModel.setListProductByCategory(it.id)
        }

        prAdapter.onClick {
            if (it.id == -1) {
                addNewProduct()
            } else {
                editProduct(it)
            }
        }

        binding.tvManagerCategories.setOnClickListener {
            val fm = childFragmentManager
            CategoriesManageFragment().show(fm, null)
        }
    }

    private fun initViews() {
        val manager = GridLayoutManager(requireActivity(), 4)
        binding.rvManagerProduct.apply {
            layoutManager = manager
            adapter = prAdapter
        }
        binding.rvCategories.apply {
            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            adapter = cateAdapter
        }
    }

    private fun addNewProduct() {
        val fm = childFragmentManager
        ProductFormFragment().show(fm, null)
    }

    private fun editProduct(product: ProductEntity) {
        val fm = childFragmentManager
        val producFormFragment = ProductFormFragment()
        val productGson = GsonUtils.getGsonParser().toJson(product)
        producFormFragment.arguments = bundleOf(
            PRODUCT_EXTRA_KEY to productGson
        )
        producFormFragment.show(fm, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}