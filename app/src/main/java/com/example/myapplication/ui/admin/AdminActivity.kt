package com.example.myapplication.ui.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.core.TabItem
import com.example.myapplication.databinding.ActivityAdminBinding
import com.example.myapplication.ui.adapter.TabItemAdapter
import com.example.myapplication.ui.adapter.TabPagerAdapter

class AdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminBinding
    private val tabAdapter = TabItemAdapter(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        initTabs()
        initPager()
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
        tabAdapter.setOnClick {
            binding.vpPages.currentItem = it
        }
    }
}