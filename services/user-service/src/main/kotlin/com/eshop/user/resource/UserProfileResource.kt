package com.eshop.user.resource

import com.eshop.user.dto.AddBalanceRequest
import com.eshop.user.dto.BalanceResponse
import com.eshop.user.dto.UserResponse
import com.eshop.user.repository.UserRepository
import com.eshop.user.service.UserService
import io.quarkus.security.Authenticated
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.bson.types.ObjectId
import org.eclipse.microprofile.jwt.JsonWebToken

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
class UserProfileResource(private val userRepository: UserRepository, private val userService: UserService, private val jwt: JsonWebToken) {

    @GET
    @Path("/me")
    @RolesAllowed("USER")
    fun me(): Response {
        val userId = jwt.subject

        val user = userRepository.findById(ObjectId(userId)) ?: return Response.status(Response.Status.NOT_FOUND).build()

        return Response.ok(UserResponse(id = user.id.toString(), username = user.username, role = user.role)).build()
    }


    @GET
    @Path("/me/balance")
    @Authenticated
    fun getBalance(): BalanceResponse {
        val username = jwt.name

        return BalanceResponse(balanceInCents = userService.getBalance(username))
    }

    @POST
    @Path("/me/balance/add")
    @Authenticated
    fun addBalance(request: AddBalanceRequest): BalanceResponse {
        val username = jwt.name

        val newBalance = userService.addBalance(username, request.amountInCents)

        return BalanceResponse(balanceInCents = newBalance)
    }
}