package com.example.grocery.product

data class ProductResponse(
    val id: String,
    val name: String,
    val priceInCents: Long
) {
}