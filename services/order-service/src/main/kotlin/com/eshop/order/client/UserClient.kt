package com.eshop.order.client

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@ApplicationScoped
@Path("/internal/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "user-api")
interface UserClient {

    @POST
    @Path("/{userId}/wallet/charge")
    fun chargeWallet(@PathParam("userId") userId: String, request: WalletChargeRequest): WalletChargeResponse
}

