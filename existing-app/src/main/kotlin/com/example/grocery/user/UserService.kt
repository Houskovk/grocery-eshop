package com.example.grocery.user

import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class UserService(private val userRepository: UserRepository) {

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
}