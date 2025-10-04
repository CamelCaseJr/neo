package org.acme.ia.controller;

import org.acme.ia.dto.PrevisaoResponse;
import org.acme.ia.service.PrevisaoPerigosidadeService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/ia/previsao")
public class PrevisaoController {

    @Inject
    PrevisaoPerigosidadeService previsaoService;

    @GET
    @Path("/perigosidade")
    @Produces(MediaType.APPLICATION_JSON)
    public PrevisaoResponse preverPerigosidade(
            @QueryParam("magnitude") Double magnitudeAbsoluta,
            @QueryParam("diametro") Double diametroMaxM,
            @QueryParam("velocidade") Double velocidadeKmS) {

        boolean ehPerigoso = previsaoService.preverPerigosidade(
            magnitudeAbsoluta, diametroMaxM, velocidadeKmS);
        
        double scoreRisco = previsaoService.calcularScoreRisco(
            magnitudeAbsoluta, diametroMaxM, velocidadeKmS);

        String justificativa = String.format(
            "Análise baseada em: Magnitude=%.2f, Diâmetro=%.2f m, Velocidade=%.2f km/s. Score de risco: %.1f%%",
            magnitudeAbsoluta, diametroMaxM, velocidadeKmS, scoreRisco
        );

        return new PrevisaoResponse(ehPerigoso, scoreRisco, justificativa);
    }
}
