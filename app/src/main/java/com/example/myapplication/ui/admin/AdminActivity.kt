package com.example.myapplication.ui.admin

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.BaseActivity
import com.example.myapplication.LoginActivity
import com.example.myapplication.core.TabItem
import com.example.myapplication.core.utils.showDialogConfirmLogout
import com.example.myapplication.databinding.ActivityAdminBinding
import com.example.myapplication.ext.gotoLogin
import com.example.myapplication.ext.showToast
import com.example.myapplication.ui.adapter.TabItemAdapter
import com.example.myapplication.ui.adapter.TabPagerAdapter
import com.example.myapplication.viewmodel.AdminViewModel

class AdminActivity : BaseActivity() {
    lateinit var binding: ActivityAdminBinding
    private val tabAdapter = TabItemAdapter(this)
    private val adminVM: AdminViewModel by lazy {
        ViewModelProvider(this)[AdminViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        initTabs()
        initPager()
        adminVM.initViewModel()
    }

    private fun initPager() {
        val tabs = TabItem.values().toList()
        val pagerAdapter = TabPagerAdapter(supportFragmentManager, tabs)
        binding.vpPages.adapter = pagerAdapter
        binding.vpPages.offscreenPageLimit = tabs.size

    }

    private fun initTabs() {
        val listTab = TabItem.values().toMutableList()
        binding.rvTab.apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = tabAdapter
        }
        tabAdapter.setData(listTab)
        tabAdapter.setSelectedItem(adminVM.pageSelected)
        tabAdapter.setOnClick {
            binding.vpPages.currentItem = it
            adminVM.pageSelected = it
        }
        binding.ivLogout.setColorFilter(
            Color.parseColor("#F44336"),
            PorterDuff.Mode.SRC_IN
        )
        binding.ivLogout.setOnClickListener {
            showDialogConfirmLogout {
                if (it.isNotBlank()) {
                    adminVM.logout(it) { b, mess ->
                        if (b) {
                            gotoLogin()
                        } else {
                            showToast(mess)
                        }
                    }
                } else {
                    showToast("Paswword is Blank")
                }
            }
        }
    }
}