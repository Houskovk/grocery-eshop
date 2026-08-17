package com.example.grocery.user

import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepository : PanacheMongoRepository<User> {
    fun findByUsername(username: String): User? {
        return find("username", username).firstResult()
    }
}