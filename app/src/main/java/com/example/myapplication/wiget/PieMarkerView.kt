package com.example.myapplication.wiget

import android.content.Context
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.ext.formatWithCurrency
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF


class PieMarkerView(context: Context, resid: Int) : MarkerView(context, resid) {
    var tv_time: TextView
    var tv_revenue: TextView

    //    private val data = arrayListOf<>()
    init {
        tv_time = findViewById(R.id.tv_time)
        tv_revenue = findViewById(R.id.tv_revenue)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            it.data?.let { data ->
                val pair = data as Pair<String, Int>
                tv_time.text = pair.first
                tv_revenue.text = pair.second.formatWithCurrency()
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }

    fun setData(data: String) {

    }
}