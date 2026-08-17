package com.eshop.order.client

data class WalletChargeResponse(
    val userId: String,
    val orderReference: String,
    val balanceInCents: Long,
    val alreadyProcessed: Boolean
)

