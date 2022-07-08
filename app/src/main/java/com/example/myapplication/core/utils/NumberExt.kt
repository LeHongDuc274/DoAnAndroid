package com.example.myapplication.ext

import android.util.Log
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun Int.formatWithCurrency(): String {
    val lc = Locale("nv", "VN") //Định dạng locale việt nam
    val nf: NumberFormat = NumberFormat.getCurrencyInstance(lc)
    return nf.format(this)
}

//fun Int.formatCalendar() : String{
//    val cal = Calendar.getInstance()
//    cal.timeInMillis = this.toLong()*1000
//    Log.e("tagXX",this.toLong().toString())
//    val sdf = SimpleDateFormat("dd/MM", Locale.JAPAN)
//    return sdf.format(cal.time)
//}