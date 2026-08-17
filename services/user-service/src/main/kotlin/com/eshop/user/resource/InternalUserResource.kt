package com.eshop.user.resource

import com.eshop.user.dto.UpdateBalanceRequest
import com.eshop.user.dto.UserBalanceResponse
import com.eshop.user.dto.WalletChargeRequest
import com.eshop.user.dto.WalletChargeResponse
import com.eshop.user.service.UserService
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.core.MediaType

@Path("/internal/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class InternalUserResource(private val userService: UserService) {

    @GET
    @Path("/{id}")
    fun getUser(@PathParam("id") id: String): UserBalanceResponse {
        val user = userService.getUserById(id)

        return UserBalanceResponse(
            id = user.id.toString(),
            username = user.username,
            balanceInCents = user.balanceInCents
        )
    }

    @PUT
    @Path("/{id}/balance")
    fun updateBalance(@PathParam("id") id: String, request: UpdateBalanceRequest): UserBalanceResponse {
        val user = userService.updateBalance(id, request.balanceInCents)

        return UserBalanceResponse(
            id = user.id.toString(),
            username = user.username,
            balanceInCents = user.balanceInCents
        )
    }

    @POST
    @Path("/{id}/wallet/charge")
    fun chargeWallet(@PathParam("id") id: String, request: WalletChargeRequest): WalletChargeResponse {
        return userService.chargeWallet(id, request.amountInCents, request.orderReference)
    }
}


