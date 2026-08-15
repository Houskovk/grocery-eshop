package com.example.grocery.checkout.dto

data class CheckoutResponse(
    val orderId: String,
    val totalInCents: Long,
    val remainingBalanceInCents: Long,
    val message: String
)