package com.example.grocery.client

import com.example.grocery.client.dto.LoginRequest
import com.example.grocery.client.dto.LoginResponse
import com.example.grocery.client.dto.RegisterRequest
import com.example.grocery.client.dto.UserResponse
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@ApplicationScoped
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "user-api")
interface AuthClient {

    @POST
    @Path("/register")
    fun register(request: RegisterRequest): UserResponse

    @POST
    @Path("/login")
    fun login(request: LoginRequest): LoginResponse
}

