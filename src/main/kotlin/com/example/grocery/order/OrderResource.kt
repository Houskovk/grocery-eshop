package com.example.grocery.order

import com.example.grocery.order.dto.OrderResponse
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