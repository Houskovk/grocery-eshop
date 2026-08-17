package com.eshop.order.dto

data class CartResponse(
    val items: List<CartItemResponse>,
    val total: Long
)

