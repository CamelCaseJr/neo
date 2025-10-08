package org.acme.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.acme.controller.NeoWsClient;
import org.acme.domain.dtos.FeedResponse;
import org.acme.domain.models.NeoObject;
import org.acme.repository.NeoRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hibernate.HibernateException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
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
            String s3key = minioService.salvarCsvBruto(json, inicio);

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
        DateTimeFormatter nasaFmt = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm", Locale.US);

        for (Map.Entry<String, List<FeedResponse.Neo>> dia : feed.nearEarthObjects.entrySet()) {
            for (FeedResponse.Neo n : dia.getValue()) {

                NeoObject ent = safeFindByNeoId(n.id);
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
                        ent.velocidadeKmS = (ca.relativeVelocity != null)
                                ? Double.parseDouble(ca.relativeVelocity.kmPerSec)
                                : null;
                    } catch (NumberFormatException ex) {
                        Log.warnf(ex, "Velocidade inválida para NEO %s: '%s'", n.id,
                                (ca.relativeVelocity != null ? ca.relativeVelocity.kmPerSec : "null"));
                    }

                    // "2025-Sep-30 12:34" -> OffsetDateTime (UTC)
                    String ad = ca.approachDateFull;
                    if (ad != null && !ad.isBlank()) {
                        try {
                            var ldt = LocalDateTime.parse(ad, nasaFmt);
                            ent.dataPrimeiraAproximacao = OffsetDateTime.of(ldt, ZoneOffset.UTC);
                        } catch (RuntimeException ex) {
                            Log.warnf(ex, "Data 'approachDateFull' inválida para NEO %s: '%s'", n.id, ad);
                            // mantém null para não poluir dado com 'now'
                        }
                    }
                }

                // Rastreabilidade da origem
                ent.origemJsonS3Key = s3key;

                // Upsert
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

    /**
     * Busca segura por NEO via neoId.
     * - Valida parâmetro (null/blank).
     * - Captura exceções de infraestrutura (JPA/Hibernate/Panache).
     * - Loga com contexto e retorna null em caso de falha para não interromper o
     * lote.
     */
    private NeoObject safeFindByNeoId(String neoId) {
        if (neoId == null || neoId.isBlank()) {
            Log.warn("safeFindByNeoId chamado com neoId nulo ou em branco; seguirá como novo registro.");
            return null;
        }
        try {
            return neoRepo.find("neoId", neoId).firstResult();
        } catch (IllegalArgumentException | PersistenceException ex) {
            Log.errorf(ex,
                    "Falha ao consultar NEO por neoId='%s'. O processamento seguirá criando/atualizando sem lookup.",
                    neoId);
            return null; // degrade com segurança: não para o lote
        }catch (RuntimeException ex) {
            Log.errorf(ex,
                    "Erro de infraestrutura ao consultar NEO por neoId='%s'. O processamento seguirá criando/atualizando sem lookup.",
                    neoId);
            return null; // degrade com segurança: não para o lote
        }
    }
}
