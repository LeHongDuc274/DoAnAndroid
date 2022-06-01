package com.example.myapplication.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.R
import com.example.myapplication.wiget.BaseDialogFragment

class CategoriesManageFragment() : BaseDialogFragment(R.layout.fragment_categories_manage) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val status = 1
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}