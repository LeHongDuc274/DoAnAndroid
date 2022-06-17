package com.example.myapplication.core.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder


object GsonUtils {
    private var gson: Gson? = null

    fun getGsonParser(): Gson {
        if (null == gson) {
            val builder = GsonBuilder()
            gson = builder.create()
        }
        return gson!!
    }
}