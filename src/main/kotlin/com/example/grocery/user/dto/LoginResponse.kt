package com.example.grocery.user.dto

data class LoginResponse(
    val token: String,
    val username: String
)