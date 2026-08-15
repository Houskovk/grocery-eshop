package com.example.grocery.user

import com.example.grocery.user.dto.LoginRequest
import com.example.grocery.user.dto.LoginResponse
import com.example.grocery.user.dto.RegisterRequest
import com.example.grocery.user.dto.UserResponse
import jakarta.ws.rs.Produces
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UserResource(private val userService: UserService, private val jwtService: JwtService) {

    @POST
    @Path("/register")
    fun register(request: RegisterRequest): Response {

        val user = userService.register(request.username, request.password)

        val response = UserResponse(
            id = user.id.toString(),
            username = user.username,
            role = user.role
        )

        return Response.status(Response.Status.CREATED).entity(response).build()
    }

    @POST
    @Path("/login")
    fun login(request: LoginRequest): Response {

        val user = userService.authenticate(request.username, request.password)
            ?: return Response.status(Response.Status.UNAUTHORIZED).build()

        val token = jwtService.generateToken(user)

        return Response.ok(LoginResponse(token = token, username = user.username)).build()
    }
}