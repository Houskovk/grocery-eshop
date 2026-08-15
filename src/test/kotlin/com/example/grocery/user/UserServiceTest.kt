package com.example.grocery.user

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@QuarkusTest
class UserServiceTest {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var userRepository: UserRepository

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
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
}

