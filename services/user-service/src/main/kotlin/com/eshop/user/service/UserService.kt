package com.eshop.user.service

import com.eshop.user.dto.WalletChargeResponse
import com.eshop.user.model.User
import com.eshop.user.model.WalletTransaction
import com.eshop.user.repository.UserRepository
import com.eshop.user.repository.WalletTransactionRepository
import com.mongodb.MongoWriteException
import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.bson.types.ObjectId

@ApplicationScoped
class UserService(
    private val userRepository: UserRepository,
    private val walletTransactionRepository: WalletTransactionRepository
) {

    fun register(username: String, password: String): User {

        if (username.isBlank()) {
            throw IllegalArgumentException("Username cannot be blank")
        }

        if (password.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters long")
        }

        val existingUser = userRepository.findByUsername(username)

        if (existingUser != null) {
            throw IllegalArgumentException("Username already exists")
        }

        val passwordHash = BcryptUtil.bcryptHash(password)

        val newUser = User(username = username, passwordHash = passwordHash, role = "USER")

        userRepository.persist(newUser)

        return newUser
    }

    fun authenticate(username: String, password: String): User? {

        val user = userRepository.findByUsername(username) ?: return null

        val passwordMatches = BcryptUtil.matches(password, user.passwordHash)

        if (!passwordMatches) {
            return null
        }

        return user
    }

    fun getBalance(username: String): Long {
        val user = userRepository.findByUsername(username) ?: throw NotFoundException("User not found")

        return user.balanceInCents
    }

    fun addBalance(username: String, amountInCents: Long): Long {
        if (amountInCents <= 0) {
            throw BadRequestException("Amount must be greater than zero")
        }

        val user = userRepository.findByUsername(username) ?: throw NotFoundException("User not found")

        val newBalance = try {
            Math.addExact(user.balanceInCents, amountInCents)
        } catch (e: ArithmeticException) {
            throw BadRequestException("Balance would be too large")
        }

        user.balanceInCents = newBalance
        userRepository.update(user)

        return user.balanceInCents
    }

    fun getUserById(userId: String): User {

        val objectId = try {
            org.bson.types.ObjectId(userId)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Invalid user ID format")
        }

        val user = userRepository.findById(objectId) ?: throw NotFoundException("User not found")
        return user
    }

    fun updateBalance(userId: String, newBalanceInCents: Long): User {
        val user = getUserById(userId)
        user.balanceInCents = newBalanceInCents
        userRepository.update(user)
        return user
    }

    fun chargeWallet(userId: String, amountInCents: Long, orderReference: String): WalletChargeResponse {
        if (amountInCents <= 0) {
            throw BadRequestException("Amount must be greater than zero")
        }

        if (orderReference.isBlank()) {
            throw BadRequestException("orderReference must not be blank")
        }

        val objectId = try {
            ObjectId(userId)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Invalid user ID format")
        }

        walletTransactionRepository.findByOrderReference(orderReference)?.let {
            val user = userRepository.findById(objectId) ?: throw NotFoundException("User not found")
            return WalletChargeResponse(
                userId = userId,
                orderReference = orderReference,
                balanceInCents = user.balanceInCents,
                alreadyProcessed = true
            )
        }

        val user = userRepository.findById(objectId) ?: throw NotFoundException("User not found")

        if (user.balanceInCents < amountInCents) {
            throw InsufficientFundsException("User $userId has insufficient balance to charge $amountInCents cents")
        }

        try {
            walletTransactionRepository.persist(
                WalletTransaction(userId = objectId, orderReference = orderReference, amountInCents = amountInCents)
            )
        } catch (e: MongoWriteException) {
            val current = userRepository.findById(objectId) ?: throw NotFoundException("User not found")
            return WalletChargeResponse(
                userId = userId,
                orderReference = orderReference,
                balanceInCents = current.balanceInCents,
                alreadyProcessed = true
            )
        }

        user.balanceInCents -= amountInCents
        userRepository.update(user)

        return WalletChargeResponse(
            userId = userId,
            orderReference = orderReference,
            balanceInCents = user.balanceInCents,
            alreadyProcessed = false
        )
    }
}