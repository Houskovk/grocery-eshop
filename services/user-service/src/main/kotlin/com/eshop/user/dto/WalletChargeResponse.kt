package com.eshop.user.dto

data class WalletChargeResponse(
    val userId: String,
    val orderReference: String,
    val balanceInCents: Long,
    val alreadyProcessed: Boolean
)

