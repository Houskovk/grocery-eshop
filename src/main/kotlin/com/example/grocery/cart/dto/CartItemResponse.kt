package com.example.grocery.cart.dto

data class CartItemResponse(
    val productId: String,
    val name: String,
    val price: Long,
    val quantity: Int,
    val lineTotal: Long
)
