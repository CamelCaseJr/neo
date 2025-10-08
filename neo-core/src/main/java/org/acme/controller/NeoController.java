package org.acme.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.Media;

import org.acme.domain.dtos.NeoObjectResponse;
import org.acme.domain.mapper.NeoObjectMapper;
import org.acme.domain.models.NeoObject;
import org.acme.service.NeoService;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/api/neos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NeoController {

    @Inject
    NeoService neoService;

    @Inject
    NeoObjectMapper neoMapper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(@QueryParam("pagina") @DefaultValue("0") int pagina,
            @QueryParam("tamanho") @DefaultValue("20") int tamanho,
            @QueryParam("perigoso") Boolean perigoso,
            @Context UriInfo uriInfo) {

        List<NeoObject> lista = (perigoso != null)
                ? neoService.listarPerigosos(pagina, tamanho, perigoso)
                : neoService.listar(pagina, tamanho);

        List<NeoObjectResponse> resp = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            NeoObject neo = lista.get(i);
            URI selfUri = uriInfo.getBaseUriBuilder()
                    .path("api/neos")
                    .path(String.valueOf(neo.id))
                    .build();
            NeoObjectResponse respInterno = neoMapper.toResponse(neo, selfUri);
            // Substitui o NeoObject pelo NeoObjectResponse na lista
            // Isso é um pouco hacky, mas funciona para este exemplo
            // Em um caso real, você provavelmente retornaria uma lista de NeoObjectResponse
            // diretamente
            resp.add(respInterno);
        }

        return Response.ok().entity(resp).build();
    }

    @GET
    @Path("/{id}")
    public Response obter(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        NeoObject ent = neoService.obterPorId(id);
        if (ent == null)
            throw new NotFoundException("NEO não encontrado");

        URI selfUri = uriInfo.getBaseUriBuilder()
                .path("api/neos")
                .path(String.valueOf(ent.id))
                .build();

        NeoObjectResponse resp = neoMapper.toResponse(ent, selfUri);

        return Response.ok(resp).build();
    }

    @POST
    @Transactional
    public Response criar(NeoObject neo, @Context UriInfo uriInfo) {
        neoService.criar(neo);

        URI self = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(neo.id))
                .build();

        NeoObjectResponse resp = neoMapper.toResponse(neo, self);

        return Response.created(self)
                .entity(resp)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response atualizar(@PathParam("id") Long id, NeoObject neo, @Context UriInfo uriInfo) {
        NeoObject ent = neoService.obterPorId(id, neo);

        URI uri = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(ent.id))
                .build();

        return Response.accepted(uri)
                .entity(ent)
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response remover(@PathParam("id") Long id) {
        if (!neoService.deleteById(id))
            throw new NotFoundException("NEO não encontrado");

        return Response.noContent().build(); // 204
    }

    @POST
    @Path("/importar")
    public Response importar(@QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim) {
        LocalDate i = LocalDate.parse(inicio);
        LocalDate f = LocalDate.parse(fim);
        int qtd = neoService.importarFeed(i, f);

        return Response.ok(Map.of("importados", qtd)).build();
    }
}
