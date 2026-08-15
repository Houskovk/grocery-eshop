package com.example.grocery.checkout

import com.example.grocery.cart.CartService
import com.example.grocery.checkout.dto.CheckoutResponse
import com.example.grocery.order.OrderItem
import com.example.grocery.order.OrderService
import com.example.grocery.product.ProductService
import com.example.grocery.user.UserService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class CheckoutService(
    private val cartService: CartService,
    private  val productService: ProductService,
    private  val userService: UserService,
    private val orderService: OrderService
) {
    fun checkout(userId: String): CheckoutResponse {

        val user = userService.getUserById(userId)

        val cart = cartService.getCart(userId)

        if (cart.items.isEmpty()) {
            throw BadRequestException("Cart is empty")
        }

        val orderItems = cart.items.map { cartItem ->
            val product = productService.getProductById(cartItem.productId) ?: throw NotFoundException("Product not found: ${cartItem.productId}")

            val subtotal = product.priceInCents * cartItem.quantity

            OrderItem(
                productId = product.id,
                quantity = cartItem.quantity,
                unitPriceInCents = product.priceInCents,
                subtotalInCents = subtotal
            )
        }

        val total = orderItems.sumOf { it.subtotalInCents }

        if (user.balanceInCents < total) {
            throw BadRequestException("Insufficient balance")
        }

        user.balanceInCents -= total

        userService.updateBalance(userId, user.balanceInCents)

        val order = orderService.createOrder(userId, orderItems, total)

        cartService.clearCart(userId)

        return CheckoutResponse(
            orderId = order.id.toString(),
            totalInCents = total,
            remainingBalanceInCents = user.balanceInCents,
            message = "Purchase completed successfully"
        )
    }
}