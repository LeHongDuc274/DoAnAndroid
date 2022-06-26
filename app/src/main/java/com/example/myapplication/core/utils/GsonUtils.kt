package com.example.myapplication.core.utils

import com.example.myapplication.core.COMMAND
import com.example.myapplication.core.IDENTIFIER
import com.example.myapplication.core.SUBSCRIBE
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject


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

fun getChannelJson(channelFormat: String ,value: String ="" ): String{
    val param = JSONObject()
    param.put(COMMAND, SUBSCRIBE)
    val param2 = JSONObject()
    param2.put("channel", String.format(channelFormat,value))
    param.put(IDENTIFIER, param2.toString())
    return param.toString()
}
