package com.example.myapplication.utils.wiget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner

class MySpinner(context: Context, attrs: AttributeSet) : AppCompatSpinner(context, attrs) {
    private var lastPosition = 0

    override fun setSelection(position: Int) {
        super.setSelection(position)
        val sameSelected = lastPosition == selectedItemPosition
        val onItemSelectedListener = onItemSelectedListener
        if (sameSelected && onItemSelectedListener != null) {
            onItemSelectedListener.onItemSelected(this, selectedView, position, selectedItemId)
        }
        lastPosition = position
    }
}