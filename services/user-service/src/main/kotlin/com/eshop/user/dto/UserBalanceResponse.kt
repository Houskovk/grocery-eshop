package com.eshop.user.dto

data class UserBalanceResponse(
    val id: String,
    val username: String,
    val balanceInCents: Long
)

