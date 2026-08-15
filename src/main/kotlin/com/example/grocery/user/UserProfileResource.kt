package com.example.grocery.user

import com.example.grocery.user.dto.UserResponse
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.bson.types.ObjectId
import org.eclipse.microprofile.jwt.JsonWebToken

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
class UserProfileResource(private val userRepository: UserRepository, private val jwt: JsonWebToken) {

    @GET
    @Path("/me")
    @RolesAllowed("USER")
    fun me(): Response {
        val userId = jwt.subject

        val user = userRepository.findById(ObjectId(userId)) ?: return Response.status(Response.Status.NOT_FOUND).build()

        return Response.ok(UserResponse(id = user.id.toString(), username = user.username, role = user.role)).build()
    }
}