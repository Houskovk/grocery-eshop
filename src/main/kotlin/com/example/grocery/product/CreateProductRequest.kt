package com.example.grocery.product

data class CreateProductRequest(
    val name: String,
    val priceInCents: Long
)