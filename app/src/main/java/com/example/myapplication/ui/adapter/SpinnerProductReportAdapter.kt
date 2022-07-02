package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.myapplication.databinding.CategorySpinnerItemBinding

class SpinnerProductReportAdapter(context: Context, listData: List<String>) :
    ArrayAdapter<String>(context, 0, listData) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, parent)
    }

    private fun createItemView(position: Int, parent: ViewGroup): View {
        val data = getItem(position)
        val binding =
            CategorySpinnerItemBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.tvCategoryName.text = data?: "none"
        return binding.root
    }
}