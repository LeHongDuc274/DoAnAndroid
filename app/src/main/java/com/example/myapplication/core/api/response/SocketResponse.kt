package com.example.myapplication.core.api.response

data class SocketResponse(
    var type: String = "",
    var identifier: String = "",
    var message: Any? = null
)