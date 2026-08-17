package com.eshop.order.dto

data class AddCartItemRequest(
    val productId: String = "",
    val quantity: Int = 1
)

