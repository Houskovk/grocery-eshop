package com.example.grocery

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/hello")
class HelloResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello(): String = "Hello from Grocery E-Shop Again!"

}