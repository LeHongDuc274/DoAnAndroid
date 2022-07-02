package com.example.myapplication.wiget

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import com.example.myapplication.databinding.MarkerViewBinding
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

class PieMarkerView(context: Context, resid: Int) : MarkerView(context, resid) {

    var binding: MarkerViewBinding

    init {
        binding = MarkerViewBinding.inflate(LayoutInflater.from(context))
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        Log.e("tagCheck", e?.y.toString())
        binding.tvName.text = e?.y.toString()
       // super.refreshContent(e, highlight)
    }

    fun getXOffset(): Int {
        // this will center the marker-view horizontally
        return -(width / 2)
    }

    fun getYOffset(): Int {
        // this will cause the marker-view to be above the selected value
        return -height
    }
}