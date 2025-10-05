package org.acme.ia.controller;

import java.time.LocalDate;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.acme.ia.service.MLInferenceService;
import org.acme.ia.service.MLTrainingService;

@Path("/ml")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MLResource {

    @Inject MLTrainingService training;
    @Inject MLInferenceService inference;

    @POST
    @Path("/train/all")
    public MLTrainingService.TrainingResult treinarComTudo() throws Exception {
        return training.treinarComTodosBuckets();
    }

    @POST
    @Path("/train")
    public MLTrainingService.TrainingResult treinar(TrainRequest req) throws Exception {
        if (req == null || req.inicio == null || req.fim == null) {
            throw new BadRequestException("Informe 'inicio' e 'fim' (YYYY-MM-DD).");
        }
        return training.treinar(req.inicio, req.fim);
    }

    @POST
    @Path("/reload")
    public String reload() throws Exception {
        inference.carregarModeloMaisRecente();
        return "ok";
    }

    @POST
    @Path("/predict")
    public MLInferenceService.PredictionResult predict(MLInferenceService.FeaturesInput in) throws Exception {
        return inference.predict(in);
    }

    // ===== DTO de entrada do /train =====
    public static class TrainRequest {
        public LocalDate inicio;
        public LocalDate fim;
    }
}
