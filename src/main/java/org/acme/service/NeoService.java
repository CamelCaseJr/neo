package org.acme.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.acme.controller.NeoWsClient;
import org.acme.domain.dtos.FeedResponse;
import org.acme.domain.models.NeoObject;
import org.acme.repository.NeoRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class NeoService {

    @Inject
    @RestClient
    NeoWsClient neoClient;
    @ConfigProperty(name = "nasa.api.key")
    String apiKey;
    @Inject
    NeoRepository neoRepo;
    @Inject
    ObjectMapper mapper;
    @Inject
    ArmazenamentoMinioService minioService;

    @Transactional
    public int importarFeed(LocalDate inicio, LocalDate fim) {
        String start = inicio.format(DateTimeFormatter.ISO_DATE);
        String end = fim.format(DateTimeFormatter.ISO_DATE);
        FeedResponse feed = neoClient.buscarFeed(start, end, apiKey);

        try {
            String json = mapper.writeValueAsString(feed);
            String s3key = minioService.salvarJsonBruto(json, inicio);

            int inseridos = normalizarPersistir(feed, s3key);

            Log.infof("Importação finalizada. Total de NEOs importados: %d", inseridos);
            return inseridos;

        } catch (Exception e) {
            throw new RuntimeException("Falha ao importar/normalizar feed", e);
        }
    }

    @Transactional
    protected int normalizarPersistir(FeedResponse feed, String s3key) {
        int count = 0;
        for (Map.Entry<String, List<FeedResponse.Neo>> dia : feed.nearEarthObjects.entrySet()) {
            for (FeedResponse.Neo n : dia.getValue()) {
                NeoObject ent = neoRepo.find("neoId", n.id).firstResult();
                if (ent == null) {
                    ent = new NeoObject();
                    ent.neoId = n.id;
                }
                ent.nome = n.name;
                ent.magnitudeAbsoluta = n.absoluteMagnitudeH;
                if (n.estimatedDiameter != null && n.estimatedDiameter.meters != null) {
                    ent.diametroMinM = n.estimatedDiameter.meters.min;
                    ent.diametroMaxM = n.estimatedDiameter.meters.max;
                }
                ent.ehPotencialmentePerigoso = n.hazardous;
                if (n.closeApproachData != null && !n.closeApproachData.isEmpty()) {
                    var ca = n.closeApproachData.get(0);
                    ent.planetaAlvo = ca.orbitingBody;
                    try {
                        ent.velocidadeKmS = ca.relativeVelocity != null
                                ? Double.parseDouble(ca.relativeVelocity.kmPerSec)
                                : null;
                    } catch (NumberFormatException ignore) {
                    }
                    // approachDateFull example: "2025-Sep-30 12:34"
                    try {
                        String ad = ca.approachDateFull;
                        if (ad != null) {
                            // naive parse fallback
                            ent.dataPrimeiraAproximacao = OffsetDateTime.now(); // simplificação; pode converter
                                                                                // corretamente depois
                        }
                    } catch (Exception ignore) {
                    }
                }
                ent.origemJsonS3Key = s3key;
                Panache.getEntityManager().merge(ent);
                count++;
            }
        }
        return count;
    }

    public List<NeoObject> listarPerigosos(int pagina, int tamanho, Boolean perigoso) {
        return neoRepo.find("ehPotencialmentePerigoso", perigoso).page(pagina, tamanho).list();
    }

    public List<NeoObject> listar(int pagina, int tamanho) {
        return neoRepo.findAll().page(pagina, tamanho).list();
    }
    @Transactional
    public boolean deleteById(Long id) {
        NeoObject ent = neoRepo.findById(id);
        if (ent != null) {
            neoRepo.delete(ent);
            return true;
        }
        return false;
    }

    public NeoObject obterPorId(Long id, NeoObject neo) {
        NeoObject ent = neoRepo.findById(id);
        if (ent == null)
            throw new NotFoundException("NEO não encontrado");
        ent.nome = neo.nome;
        ent.magnitudeAbsoluta = neo.magnitudeAbsoluta;
        ent.diametroMinM = neo.diametroMinM;
        ent.diametroMaxM = neo.diametroMaxM;
        ent.ehPotencialmentePerigoso = neo.ehPotencialmentePerigoso;
        ent.dataPrimeiraAproximacao = neo.dataPrimeiraAproximacao;
        ent.velocidadeKmS = neo.velocidadeKmS;
        ent.planetaAlvo = neo.planetaAlvo;
        ent.origemJsonS3Key = neo.origemJsonS3Key;
        return ent;

    }
    @Transactional
    public void criar(NeoObject neo) {
        neoRepo.persist(neo);
    }
    public NeoObject obterPorId(Long id) {
        NeoObject neoObject = neoRepo.findById(id);
        return neoObject;
    }
}
