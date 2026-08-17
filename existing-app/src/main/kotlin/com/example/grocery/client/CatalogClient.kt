package com.example.grocery.client

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@ApplicationScoped
@Path("/products")
@RegisterRestClient(configKey = "catalog-api")
interface CatalogClient {

    @GET
    fun getProducts(): List<CatalogProductResponse>

    @GET
    @Path("/{productId}")
    fun getProduct(@PathParam("productId") productId: String): CatalogProductResponse
}