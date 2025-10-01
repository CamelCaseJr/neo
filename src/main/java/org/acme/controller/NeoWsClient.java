package org.acme.controller;

import org.acme.domain.dtos.FeedResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@RegisterRestClient(configKey = "nasa-neows")
@Path("/neo")
public interface NeoWsClient {

  @GET
  @Path("/feed")  
  public FeedResponse buscarFeed(@QueryParam("start_date") String startDate,
                            @QueryParam("end_date") String endDate,
                            @QueryParam("api_key") String apiKey);
} 
