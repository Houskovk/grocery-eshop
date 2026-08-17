package com.eshop.user.service

import com.eshop.user.model.User
import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class JwtService {

    fun generateToken(user: User): String {

        return Jwt
            .subject(user.id.toString())
            .upn(user.username)
            .groups(setOf(user.role))
            .sign()
    }
}