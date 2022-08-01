package com.example.myapplication.core

const val APP_NAME = ""
const val BASE_URL = "http://192.168.1.6:3000"
const val WS_URL = "ws://192.168.1.6:3000/cable"

const val PRODUCT_EXTRA_KEY = "PRODUCT_EXTRA_KEY"
const val USER_EXTRA_KEY = "USER_EXTRA_KEY"

const val FLAG_UPDATE_ORDER = "FLAG_UPDATE_ORDER"
const val FLAG_UPDATE_MESSAGE = "FLAG_UPDATE_MESSAGE"
const val FLAG_NEW_MESSAGE = "FLAG_NEW_MESSAGE"

enum class Role(val title: String, val code: Int) {
    ADMIN("admin", 0),
    TABLE("table", 3),
    STAFF("staff", 1),
    KITCHEN("kitchen", 2);

    fun findRoleByCode(i: Int): Role {
        return Role.values().first {
            it.code == i
        }
    }
}

enum class TabItem(val title: String, val icon: String, code: Int) {
    HOME("Home", "home", 0),
    ORDERS("Orders", "orders", 1),
    CHART("Chart", "chart", 2),
    SETTING("Settings", "setting", 3)
}

enum class ItemStatus(val title: String, val status: Int) {
    NONE("None", -1),
    PENDING("Đang chờ", 0),
    PREPARING("Đang chuẩn bị", 1),
    COMPLETED("Hoàn thành", 2),
    DELIVERING("Đang giao", 3),
    DELIVERED("Đã giao", 4)
}

const val COMMAND = "command"
const val SUBSCRIBE = "subscribe"
const val IDENTIFIER = "identifier"
const val TOKEN = "token"
const val ORDER_CHANNEL_FORMAT = "{\"channel\":\"OrderChannel\", \"user_id\": \"%d\"}"
enum class Channel(val channel: String) {
    ORDER_DETAIL_KITCHEN_CHANNEL("{\"channel\":\"OrderDetailChannel\"}"),
    MESSAGE_CHANNEL("{\"channel\":\"MessageChannel\"}"),
    PRODUCT_CHANNEL("{\"channel\":\"ProductChannel\"}"),
}

