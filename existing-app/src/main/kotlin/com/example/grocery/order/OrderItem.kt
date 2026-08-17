package com.example.grocery.order

data class OrderItem(
    var productId: String = "",
    var productName: String = "",
    var unitPriceInCents: Long = 0,
    var quantity: Int = 0,
    var subtotalInCents: Long = 0
)