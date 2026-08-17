package com.example.grocery.cart.dto

data class AddCartItemRequest(
    val productId: String = "",
    val quantity: Int = 1
)
