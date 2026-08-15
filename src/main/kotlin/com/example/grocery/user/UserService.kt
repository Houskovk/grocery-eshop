package com.example.grocery.user

import io.quarkus.elytron.security.common.BcryptUtil
import jakarta.enterprise.context.ApplicationScoped

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
}