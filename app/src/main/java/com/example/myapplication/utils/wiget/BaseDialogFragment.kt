package com.example.myapplication.utils.wiget

import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R

open class BaseDialogFragment (@LayoutRes val layoutId: Int) : DialogFragment(layoutId) {
    override fun getTheme(): Int {
        return R.style.DialogTheme
    }
}