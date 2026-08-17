package com.eshop.user.resource

import com.eshop.user.service.InsufficientFundsException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class InsufficientFundsExceptionMapper : ExceptionMapper<InsufficientFundsException> {
    override fun toResponse(exception: InsufficientFundsException): Response =
        Response.status(402) // Payment Required
            .type(MediaType.APPLICATION_JSON)
            .entity(mapOf("error" to (exception.message ?: "Insufficient funds")))
            .build()
}

