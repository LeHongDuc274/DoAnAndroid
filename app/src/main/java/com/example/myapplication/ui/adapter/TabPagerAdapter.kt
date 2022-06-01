package com.example.myapplication.ui.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.example.myapplication.core.TabItem
import com.example.myapplication.ui.admin.ChartFragment
import com.example.myapplication.ui.admin.HomeFragment
import com.example.myapplication.ui.admin.OrderManagerFragment
import com.example.myapplication.ui.admin.SettingFragment

class TabPagerAdapter(fragmentManager: FragmentManager, private val tabItems: List<TabItem>) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    @Suppress("MagicNumber")
    override fun getItem(position: Int) = when (position) {
        0 -> HomeFragment()
        1 -> OrderManagerFragment()
        2 -> ChartFragment()
        else -> SettingFragment()
    }

    override fun getCount() = tabItems.size

    override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_NONE
}