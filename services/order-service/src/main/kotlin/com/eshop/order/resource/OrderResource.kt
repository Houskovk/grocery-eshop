package com.eshop.order.resource

import com.eshop.order.dto.OrderResponse
import com.eshop.order.service.OrderService
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
class OrderResource(private val orderService: OrderService, private val jwt: JsonWebToken) {

    @GET
    @RolesAllowed("USER")
    fun getMyOrders(): List<OrderResponse> {
        val userId = jwt.subject

        return orderService.findOrdersForUser(userId).map { order ->
            OrderResponse(
                id = order.id.toString(),
                items = order.items,
                totalInCents = order.totalInCents,
                createdAt = order.createdAt
            )
        }
    }
}

