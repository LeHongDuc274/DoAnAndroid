package com.example.myapplication.core


const val APP_NAME = ""
const val BASE_URL = "http://192.168.1.2:3000"
enum class Role(val title: String, val code: Int) {
    ADMIN("admin", 1),
    TABLE("table", 2),
    STAFF("staff", 3),
    KITCHEN("kitchen", 4);

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

