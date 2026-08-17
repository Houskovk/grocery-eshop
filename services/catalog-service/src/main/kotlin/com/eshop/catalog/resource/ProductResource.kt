package com.eshop.catalog.resource

import com.eshop.catalog.dto.CreateProductRequest
import com.eshop.catalog.dto.ProductResponse
import com.eshop.catalog.service.ProductService
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
class ProductResource(private val productService: ProductService) {

    @GET
    fun getAllProducts(): List<ProductResponse> = productService.getAllProducts()

    @GET
    @Path("/{id}")
    fun getProductById(@PathParam("id") id: String): Response {
        val product = productService.getProductById(id)

        return if (product != null) {
            Response.ok(product).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun addProduct(request: CreateProductRequest): Response {
        val createdProduct = productService.addProduct(request)

        return Response
            .status(Response.Status.CREATED)
            .entity(createdProduct)
            .build()
    }
}