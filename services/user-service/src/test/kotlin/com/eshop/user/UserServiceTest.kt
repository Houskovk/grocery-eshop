package com.eshop.user

import com.eshop.user.model.User
import com.eshop.user.repository.UserRepository
import com.eshop.user.repository.WalletTransactionRepository
import com.eshop.user.service.InsufficientFundsException
import com.eshop.user.service.UserService
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class UserServiceTest {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var walletTransactionRepository: WalletTransactionRepository

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
        walletTransactionRepository.deleteAll()
    }

    private fun createTestUser(username: String, balanceInCents: Long = 0): User {
        val user = User(
            username = username,
            passwordHash = "not-used-in-this-test",
            balanceInCents = balanceInCents
        )

        userRepository.persist(user)

        return user
    }

    @Test
    fun startingBalance_shouldBeZero() {
        val username = "wallet-start-${System.nanoTime()}"

        createTestUser(username = username)

        assertEquals(0L, userService.getBalance(username))
    }

    @Test
    fun add_shouldIncreaseBalance() {
        val username = "wallet-add-${System.nanoTime()}"

        createTestUser(username = username, balanceInCents = 0)

        val result = userService.addBalance(username, 5000)

        assertEquals(5000L, result)

        val persistedUser = userRepository.findByUsername(username)

        assertEquals(5000L, persistedUser?.balanceInCents)
    }

    @Test
    fun add_shouldAccumulateBalance() {
        val username = "wallet-accumulate-${System.nanoTime()}"

        createTestUser(username = username)

        userService.addBalance(username, 1000)
        val result = userService.addBalance(username, 5000)

        assertEquals(6000L, result)

        val persistedUser = userRepository.findByUsername(username)

        assertEquals(6000L, persistedUser?.balanceInCents)
    }

    @Test
    fun add_shouldBeRejected_whenAmountIsZero() {
        val username = "wallet-zero-${System.nanoTime()}"

        createTestUser(username = username)

        assertThrows(BadRequestException::class.java) {
            userService.addBalance(username, 0)
        }

        val persistedUser = userRepository.findByUsername(username)

        assertEquals(0L, persistedUser?.balanceInCents)
    }

    @Test
    fun add_shouldBeRejected_whenAmountIsNegative() {
        val username = "wallet-negative-${System.nanoTime()}"

        createTestUser(username = username)

        assertThrows(BadRequestException::class.java) {
            userService.addBalance(username, -5000)
        }

        val persistedUser = userRepository.findByUsername(username)

        assertEquals(0L, persistedUser?.balanceInCents)
    }

    @Test
    fun chargeWallet_shouldDeductBalance() {
        val user = createTestUser(username = "wallet-charge-${System.nanoTime()}", balanceInCents = 5000)

        val result = userService.chargeWallet(user.id.toString(), 1299, "order-${System.nanoTime()}")

        assertEquals(3701L, result.balanceInCents)
        assertFalse(result.alreadyProcessed)

        val persistedUser = userRepository.findById(user.id!!)
        assertEquals(3701L, persistedUser?.balanceInCents)
    }

    @Test
    fun chargeWallet_shouldThrowInsufficientFunds_whenBalanceTooLow() {
        val user = createTestUser(username = "wallet-poor-${System.nanoTime()}", balanceInCents = 100)

        assertThrows(InsufficientFundsException::class.java) {
            userService.chargeWallet(user.id.toString(), 1299, "order-${System.nanoTime()}")
        }

        val persistedUser = userRepository.findById(user.id!!)
        assertEquals(100L, persistedUser?.balanceInCents)
    }

    @Test
    fun chargeWallet_shouldBeIdempotent_whenRetriedWithSameOrderReference() {
        val user = createTestUser(username = "wallet-idempotent-${System.nanoTime()}", balanceInCents = 5000)
        val orderReference = "order-${System.nanoTime()}"

        val first = userService.chargeWallet(user.id.toString(), 1299, orderReference)
        val second = userService.chargeWallet(user.id.toString(), 1299, orderReference)

        assertFalse(first.alreadyProcessed)
        assertTrue(second.alreadyProcessed)
        assertEquals(first.balanceInCents, second.balanceInCents)

        val persistedUser = userRepository.findById(user.id!!)
        assertEquals(3701L, persistedUser?.balanceInCents)
    }
}

