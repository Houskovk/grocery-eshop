package com.eshop.order.testsupport

import com.eshop.order.client.UserClient
import com.eshop.order.client.WalletChargeRequest
import com.eshop.order.client.WalletChargeResponse
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.WebApplicationException

class MockedUserClient : UserClient {

    private val balances = mutableMapOf<String, Long>()
    private val processedReferences = mutableMapOf<String, WalletChargeResponse>()

    fun seedUser(userId: String, balanceInCents: Long = 0) {
        balances[userId] = balanceInCents
    }

    fun getBalance(userId: String): Long =
        balances[userId] ?: throw NotFoundException("User not found: $userId")

    fun clear() {
        balances.clear()
        processedReferences.clear()
    }

    override fun chargeWallet(userId: String, request: WalletChargeRequest): WalletChargeResponse {
        processedReferences[request.orderReference]?.let { return it }

        val balance = balances[userId] ?: throw NotFoundException("User not found: $userId")

        if (balance < request.amountInCents) {
            throw WebApplicationException(402)
        }

        val newBalance = balance - request.amountInCents
        balances[userId] = newBalance

        val response = WalletChargeResponse(
            userId = userId,
            orderReference = request.orderReference,
            balanceInCents = newBalance,
            alreadyProcessed = false
        )
        processedReferences[request.orderReference] = response
        return response
    }
}

