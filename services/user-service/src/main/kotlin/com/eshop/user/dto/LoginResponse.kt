package com.eshop.user.dto

data class LoginResponse(
    val token: String,
    val username: String
)