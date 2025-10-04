package org.acme.ia;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/ia")
public class ModeloIAResource {

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public String health() {
        return "{\"status\": \"Modelo IA ativo\"}";
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public String info() {
        return "{\"module\": \"modelo-ia\", \"version\": \"1.0.0-SNAPSHOT\", \"description\": \"Módulo de IA para análise de NEOs\"}";
    }
}
