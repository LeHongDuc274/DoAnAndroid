package com.example.myapplication.ui.admin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.core.api.response.ProductReport
import com.example.myapplication.core.api.response.ReportToday
import com.example.myapplication.core.api.response.RevenueReport
import com.example.myapplication.core.model.CategoryEntity
import com.example.myapplication.core.utils.Utils
import com.example.myapplication.databinding.FragmentChartBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ext.formatWithCurrency
import com.example.myapplication.ui.adapter.ReportProductAdapter
import com.example.myapplication.ui.adapter.SpinnerCategoryAdapter
import com.example.myapplication.ui.adapter.SpinnerProductReportAdapter
import com.example.myapplication.viewmodel.AdminViewModel
import com.example.myapplication.wiget.PieMarkerView
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*


class ChartFragment : Fragment() {

    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!
    val timeLastWeek = arrayListOf<String>()
    val timeAllMonth = arrayListOf<String>()
    val productReportAdapter = ReportProductAdapter()
    private val adminVM: AdminViewModel by lazy {
        ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
    }


    private fun initListener() {
        collectFlow(adminVM.reportToday) {
            setDataReportToday(it)
        }
        collectFlow(adminVM.revenueLastWeek) {
            if (it.isNotEmpty()) {
                setDataRevenueLastWeek(it)
            }
        }
        collectFlow(adminVM.revenueAllTime) {
            if (it.isNotEmpty()) {
                setDataRevenueAllTime(it)
            }
        }
        collectFlow(adminVM.productReportByCategory) {
            if (it.isNotEmpty()) {
                setDataReportProduct(it)
            } else {
                binding.pcProductReport.clear()
                binding.clMostProductOrdered.visibility = View.INVISIBLE
            }
        }
        collectFlow(adminVM.listCategories) {
            if (it.isNotEmpty()) {
                setupSpinner()
            }
        }
        binding.tvTimeReloadToday.setOnClickListener {
            adminVM.getReportToday()
        }
        binding.tvReloadLastWeek.setOnClickListener {
            adminVM.getRevenueLastWeek()
        }
        binding.tvReloadAllTime.setOnClickListener {
            adminVM.getRevenueAllTime()
        }
        binding.icProductReport.tvViewAll.setOnClickListener {
            binding.root.openDrawer(GravityCompat.END)
            productReportAdapter.setData(adminVM.productReportByCategory.value)
            binding.tvAllProductOrdered.text =
                "Đơn hàng của " + (binding.snProductReportByCategory.selectedItem as CategoryEntity).name_type
        }

        adminVM.getReportToday()
        adminVM.getRevenueLastWeek()
        adminVM.getRevenueAllTime()
    }

    private fun setDataReportProduct(list: MutableList<ProductReport>) {
        val values = arrayListOf<PieEntry>()
        list.forEachIndexed { index, e ->
            values.add(PieEntry(e.count.toFloat(), e.product_name, e.revenue))
        }
        val pieDataset = PieDataSet(values, "")
        pieDataset.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val pieData = PieData(pieDataset)
        pieData.apply {
            setValueTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            setValueTextSize(8f)
        }
        binding.pcProductReport.apply {
            data = pieData
            animateY(500)
            invalidate()
        }
        val mostProduct = list.first()
        mostProduct.let {
            binding.clMostProductOrdered.visibility = View.VISIBLE
            val image_url = adminVM.listProducts.value.find { pr ->
                it.product == pr.id
            }?.image_url
            binding.icProductReport.apply {
                tvViewAll.visibility = View.VISIBLE
                tvAmount.text = it.count.toString() + " sản phẩm đã bán"
                tvProductName.text = it.product_name
                tvRevenue.text = it.revenue.formatWithCurrency()
                Glide.with(binding.root.context).load(image_url).into(ivProduct)
            }
        }
    }

    private fun setDataRevenueAllTime(list: MutableList<RevenueReport>) {
        val values = arrayListOf<BarEntry>()
        timeAllMonth.clear()
        list.forEachIndexed { index, revenueReport ->
            values.add(BarEntry(index.toFloat(), revenueReport.revenue / 1000.toFloat()))
            timeAllMonth.add(revenueReport.time)
        }
        val datasets = BarDataSet(values, "")
        val barData = BarData(datasets)
        datasets.color = ContextCompat.getColor(requireActivity(), R.color.teal_700)
        datasets.valueTextColor = ContextCompat.getColor(requireActivity(), R.color.white)
        datasets.valueTextSize = 8f
        barData.barWidth = 0.3f
        binding.bcRevenueAllTime.apply {
            setDrawValueAboveBar(true)
            xAxis.labelCount = timeAllMonth.size
            data = barData
            animateY(500)
            invalidate()
        }
    }

    private fun setDataRevenueLastWeek(list: MutableList<RevenueReport>) {
        val values = arrayListOf<BarEntry>()
        timeLastWeek.clear()

        list.forEachIndexed { index, revenueReport ->
            values.add(BarEntry(index.toFloat(), revenueReport.revenue.toFloat()))
            timeLastWeek.add(revenueReport.time)
        }
        val datasets = BarDataSet(values, "")
        val barData = BarData(datasets)
        datasets.color = ContextCompat.getColor(requireActivity(), R.color.teal_700)
        datasets.valueTextColor = ContextCompat.getColor(requireActivity(), R.color.white)
        barData.barWidth = 0.5f
        binding.bcRevenueLastWeek.apply {
            data = barData
            animateY(500)
            setDrawValueAboveBar(true)
            invalidate()
        }
    }

    private fun setDataReportToday(it: ReportToday) {
        binding.icRevenue.apply {
            tvTotal.text = it.total_revenue.formatWithCurrency()
            tvChangePercent.text = String.format("%.2f ", it.total_revenue_change * 100) + "%"
        }
        binding.icDishOrdered.apply {
            tvTotal.text = it.total_dish_order.toString()
            tvChangePercent.text = String.format("%.2f ", it.total_dish_order_change * 100) + "%"
        }
        binding.icCustomer.apply {
            tvTotal.text = it.total_customer.toString()
            tvChangePercent.text = String.format("%.2f ", it.total_customer_change * 100) + "%"
        }
    }

    private fun initViews() {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        binding.tvTime.text = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("GMT+7")).time)
        binding.icRevenue.tvLabel.text = "Doanh thu"
        binding.icCustomer.tvLabel.text = "Số đơn hàng"
        binding.icCustomer.ivIcon.setImageResource(R.drawable.outline_people_24)
        binding.icDishOrdered.ivIcon.setImageResource(R.drawable.outline_bookmark_added_24)
        binding.icDishOrdered.tvLabel.text = "Sản phẩm đã bán"
        binding.rvAllProductOrdered.apply {
            adapter = productReportAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }
        setupChart()
    }

    private fun setupSpinner() {
        val list = mutableListOf("Hôm nay", "Tuần này", "Toàn kỳ")
        val spinnerAdapter = SpinnerProductReportAdapter(requireActivity(), list)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.snProductReport.apply {
            adapter = spinnerAdapter
            post {
                dropDownWidth = measuredWidth
            }

            setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    adminVM.getReportProduct(p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            })

        }
        val listCategory = adminVM.listCategories.value
        listCategory.add(0, CategoryEntity(0, "Tất cả"))
        val spinCategoryAdapter = SpinnerCategoryAdapter(requireActivity(), listCategory)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.snProductReportByCategory.apply {
            adapter = spinCategoryAdapter
            post {
                dropDownWidth = measuredWidth
            }

            setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    adminVM.filterProductReportByCategory((selectedItem as CategoryEntity).id)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) = Unit
            })
        }
    }

    private fun setupChart() {
        binding.bcRevenueLastWeek.apply {

            legend.isEnabled = false
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setDrawBorders(false)
            getDescription().setEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            xAxis.apply {
                setDrawGridLines(false)
                setDrawLimitLinesBehindData(false)
                textColor = ContextCompat.getColor(requireActivity(), R.color.white)
                granularity = 1f
                labelCount = 7
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return timeLastWeek[value.toInt()]
                        // return value.toInt().toString()
                    }
                }
            }

            axisLeft.apply {
                spaceTop = 15f
                setGranularityEnabled(true)
                setDrawZeroLine(true)
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                setDrawGridLines(false)
                setDrawLimitLinesBehindData(false)
                textColor = ContextCompat.getColor(requireActivity(), R.color.white)
            }
            axisRight.setDrawLabels(false)
        }

        binding.bcRevenueAllTime.apply {
            legend.isEnabled = false
            setDrawBarShadow(false)
            setDrawBorders(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            xAxis.apply {
                setDrawLabels(true)
                setDrawGridLines(false)
                setDrawLimitLinesBehindData(false)
                textColor = ContextCompat.getColor(requireActivity(), R.color.white)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return timeAllMonth[value.toInt()]
                    }
                }
            }
            axisRight.setDrawLabels(false)
            axisLeft.apply {
                getDescription().apply {
                    text = "Đơn vị: nghìn vnđ"
                    textColor = ContextCompat.getColor(requireActivity(), R.color.white_1)
                }
                setDrawGridLines(false)
                setDrawLimitLinesBehindData(false)
                textColor = ContextCompat.getColor(requireActivity(), R.color.white)
                spaceTop = 15f
                setGranularityEnabled(true)
                setDrawZeroLine(true)
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            }
        }

        binding.pcProductReport.apply {
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            legend.isEnabled = false
            getDescription().apply {
                text = "theo % số lượng đã bán"
                textColor = ContextCompat.getColor(requireActivity(), R.color.white_1)
                textSize = 10f
            }
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        binding.tvHighlightName.text = "${e.label}"
                        binding.tvHighlightAmount.text = "Đã bán: ${e.value.toInt()}"
                        binding.tvHighlightRevenue.text =
                            " Doanh thu: ${(e.data as Int).formatWithCurrency()}"
                    } else {
                        return
                    }
                }

                override fun onNothingSelected() = Unit

            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}