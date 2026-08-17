package com.eshop.order.resource

import com.eshop.order.dto.CheckoutResponse
import com.eshop.order.service.CheckoutService
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken

@Path("/checkout")
@Produces(MediaType.APPLICATION_JSON)
class CheckoutResource {

    @Inject
    lateinit var jwt: JsonWebToken

    @Inject
    lateinit var checkoutService: CheckoutService

    @POST
    @RolesAllowed("USER")
    fun checkout(): CheckoutResponse {

        val userId = jwt.subject

        return checkoutService.checkout(userId)
    }
}

