package com.example.grocery.client

import com.example.grocery.client.dto.UpdateBalanceRequest
import com.example.grocery.client.dto.UserBalanceResponse
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@ApplicationScoped
@Path("/internal/users")
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "user-api")
interface UserClient {

    @GET
    @Path("/{userId}")
    fun getUser(@PathParam("userId") userId: String): UserBalanceResponse

    @PUT
    @Path("/{userId}/balance")
    fun updateBalance(@PathParam("userId") userId: String, request: UpdateBalanceRequest): UserBalanceResponse
}

