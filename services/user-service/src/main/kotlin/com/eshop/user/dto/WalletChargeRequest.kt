package com.eshop.user.dto

data class WalletChargeRequest(
    val amountInCents: Long,
    val orderReference: String
)

