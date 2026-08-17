package com.eshop.order.client

data class WalletChargeRequest(
    val amountInCents: Long,
    val orderReference: String
)
