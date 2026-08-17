package com.example.grocery.testsupport

import com.example.grocery.client.dto.UpdateBalanceRequest
import com.example.grocery.client.dto.UserBalanceResponse
import com.example.grocery.client.UserClient
import jakarta.ws.rs.NotFoundException

class MockedUserClient : UserClient {

    private val users = mutableMapOf<String, UserBalanceResponse>()

    fun seedUser(userId: String, balanceInCents: Long = 0, username: String = "user-$userId"): UserBalanceResponse {
        val user = UserBalanceResponse(id = userId, username = username, balanceInCents = balanceInCents)
        users[userId] = user
        return user
    }

    fun clear() {
        users.clear()
    }

    override fun getUser(userId: String): UserBalanceResponse =
        users[userId] ?: throw NotFoundException("User not found: $userId")

    override fun updateBalance(userId: String, request: UpdateBalanceRequest): UserBalanceResponse {
        val existing = users[userId] ?: throw NotFoundException("User not found: $userId")
        val updated = existing.copy(balanceInCents = request.balanceInCents)
        users[userId] = updated
        return updated
    }
}

