package com.example.myapplication.core.model

data class OrderDetail(
    var id: Int = -1,
    var user_id: Int = -1,
    var order_id: Int = -1,
    var product_id: Int = -1,
    var amount: Int = 0,
    var note: String = "",
    var status: Int = -1,
    var user_display_name: String = "",
    var total_price: Int = 0,
    var created_at: String = ""
) {
    fun copyInstance(): OrderDetail {
        return OrderDetail(
            id = this.id,
            user_id = this.user_id,
            product_id = this.product_id,
            order_id = this.order_id,
            amount = this.amount,
            note = this.note,
            status = this.status,
            user_display_name = this.user_display_name,
            created_at = this.created_at
        )
    }
}
