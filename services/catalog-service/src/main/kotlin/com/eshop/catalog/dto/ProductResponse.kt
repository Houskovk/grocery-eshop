package com.eshop.catalog.dto

data class ProductResponse(
    val id: String,
    val name: String,
    val priceInCents: Long
) {
}