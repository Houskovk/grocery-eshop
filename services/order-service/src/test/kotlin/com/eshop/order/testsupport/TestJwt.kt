package com.eshop.order.testsupport

import io.smallrye.jwt.build.Jwt

object TestJwt {

    fun generate(userId: String, role: String = "USER"): String {
        return Jwt
            .subject(userId)
            .groups(setOf(role))
            .sign()
    }
}

