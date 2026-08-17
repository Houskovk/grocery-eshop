package com.example.grocery.client.dto

data class UserBalanceResponse(
    val id: String,
    val username: String,
    val balanceInCents: Long
)

