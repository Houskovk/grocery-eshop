package com.example.grocery.cart.dto

data class CartResponse(
    val items: List<CartItemResponse>,
    val total: Long
)
