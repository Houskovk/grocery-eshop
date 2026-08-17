package com.example.grocery.auth

import com.example.grocery.client.AuthClient
import com.example.grocery.client.dto.LoginRequest
import com.example.grocery.client.dto.RegisterRequest
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.inject.RestClient

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class AuthResource(
    @RestClient
    private val authClient: AuthClient
) {

    @POST
    @Path("/register")
    fun register(request: RegisterRequest): Response {
        return try {
            val user = authClient.register(request)
            Response.status(Response.Status.CREATED).entity(user).build()
        } catch (e: WebApplicationException) {
            Response.status(e.response.status).entity(readErrorBody(e)).build()
        }
    }

    @POST
    @Path("/login")
    fun login(request: LoginRequest): Response {
        return try {
            Response.ok(authClient.login(request)).build()
        } catch (e: WebApplicationException) {
            Response.status(e.response.status).build()
        }
    }

    private fun readErrorBody(e: WebApplicationException): Any? {
        return try {
            e.response.readEntity(String::class.java)
        } catch (ex: Exception) {
            null
        }
    }
}

