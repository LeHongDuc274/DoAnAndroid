package com.example.myapplication.ui.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProductsBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.ProductsAdapter
import com.example.myapplication.viewmodel.CustomerViewModel
import java.text.SimpleDateFormat
import java.util.*

class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: CustomerViewModel
    private val productAdapter = ProductsAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProductsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[CustomerViewModel::class.java]
        initView()
        initListener()
        initOnClick()

    }

    private fun initOnClick() {
        productAdapter.setOnClick {
            showBottomSheetDialog(it.id)
        }
        binding.ivIconMemu.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences(
                getString(R.string.shared_file_name), Context.MODE_PRIVATE
            ) ?: return@setOnClickListener
            with(sharedPref.edit()) {
                putString(getString(R.string.key_role), "")
                putString(getString(R.string.key_access_token), "")
                apply()
            }
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun showBottomSheetDialog(id: Int) {
        val sheet = BottomSheetProductsFragment()
        sheet.arguments = bundleOf(
            "id" to id
        )
        sheet.show(requireActivity().supportFragmentManager, id.toString())
    }

    private fun initListener() {
        collectFlow(viewModel.listProducts) {
            productAdapter.setListData(it)
            it.forEach {
                Log.e("tagPros", it.countOrder.toString())
            }
        }
    }

    private fun initView() {
        binding.rvListProduct.apply {
            layoutManager = GridLayoutManager(requireActivity(), 4)
            adapter = productAdapter
        }
        val sdf = SimpleDateFormat("dd/MM/yyyy")

        binding.tvTime.text = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("GMT+7")).time)
    }
}