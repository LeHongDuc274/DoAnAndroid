package com.example.myapplication.ui.admin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.core.PRODUCT_EXTRA_KEY
import com.example.myapplication.core.Role
import com.example.myapplication.core.USER_EXTRA_KEY
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.core.model.User
import com.example.myapplication.core.utils.GsonUtils
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
        binding.tvTable.setOnClickListener {
            changeTab(Role.TABLE.code)
        }
        binding.tvStaff.setOnClickListener {
            changeTab(Role.STAFF.code)
        }
        binding.tvKitchen.setOnClickListener {
            changeTab(Role.KITCHEN.code)
        }
        collectFlow(adminVm.listUserByRole) {
            tableAdapter.setData(it)
        }
        tableAdapter.setOnItemClick {
            Log.e("tagUser",it.toString())
            if (it.id == -1) {
                newUser()
            } else {
                editUser(it)
            }
        }
    }

    private fun editUser(it: User) {
        val fm = childFragmentManager
        val userFormFragment = UserFormFragment()
        val userJson = GsonUtils.getGsonParser().toJson(it)
        userFormFragment.arguments = bundleOf(
            USER_EXTRA_KEY to userJson
        )
        userFormFragment.show(fm, null)
    }

    private fun newUser() {
        val fm = childFragmentManager
        UserFormFragment().show(fm, null)
    }

    private fun changeTab(role: Int) {
        adminVm.getListUserByRole(role)
        binding.llTab.children.forEach {
            if (it.tag == role.toString()) {
                (it as TextView).setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.text_color_edit
                    )
                )
            } else {
                (it as TextView).setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
            }
        }
    }

    private fun initViews() {
        changeTab(Role.TABLE.code)
        binding.toolBar.tvTitle.text = "User Manage"
        binding.toolBar.tvBack.visibility = View.INVISIBLE
        binding.rvUser.apply {
            adapter = tableAdapter
            layoutManager = GridLayoutManager(requireActivity(), 4)
        }
    }

}