package com.eshop.catalog.dto

data class CreateProductRequest(
    val name: String,
    val priceInCents: Long
)