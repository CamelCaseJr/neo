package org.acme.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.acme.domain.dtos.FeedResponse;
import org.acme.domain.dtos.NeoObjectResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ApplicationScoped
public class ArmazenamentoMinioService {
    @Inject
    S3Client s3Client;
    @ConfigProperty(name = "neo.minio.bucket")
    String bucket;
    private static final DecimalFormat DF = new DecimalFormat("#.###");

    public void criarBucket() {
        try {
            // Verifica se o bucket existe
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            Log.infof("Bucket '%s' já existe.", bucket);

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                // 404 = bucket não encontrado → criar
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                    Log.infof("Bucket '%s' criado com sucesso.", bucket);
                } catch (S3Exception ce) {
                    Log.errorf(ce, "Falha ao criar o bucket '%s': %s", bucket, ce.awsErrorDetails().errorMessage());
                    throw new RuntimeException("Erro ao criar bucket: " + ce.getMessage(), ce);
                }
            } else {
                // Outro tipo de erro (ex: permissão, endpoint, credencial inválida)
                Log.errorf(e, "Erro ao verificar existência do bucket '%s': %s",
                        bucket, e.awsErrorDetails().errorMessage());
                throw new RuntimeException("Falha ao verificar bucket: " + e.getMessage(), e);
            }
        } catch (Exception ex) {
            // Falhas genéricas (problema de rede, etc.)
            Log.errorf(ex, "Erro inesperado ao acessar o bucket '%s'", bucket);
            throw new RuntimeException("Erro inesperado ao acessar bucket: " + ex.getMessage(), ex);
        }
    }

    public String salvarJsonBruto(String conteudoJson, LocalDate data) {
        criarBucket();
        String key = "raw/" + data.toString() + "/neows-feed-" + System.currentTimeMillis() + ".json";
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("application/json")
                        .build(),
                RequestBody.fromString(conteudoJson, StandardCharsets.UTF_8));
        Log.info("Arquivo salvo no MinIO com key: " + key);

        return key;
    }

    public String salvarCsvBruto(String conteudoJson, LocalDate data) throws Exception {
        criarBucket();

        // Converter JSON para objeto FeedResponse
        ObjectMapper jsonMapper = new ObjectMapper();
        FeedResponse feed = jsonMapper.readValue(conteudoJson, FeedResponse.class);

        // Achatar os dados em uma lista simples
        List<CsvNeoData> csvData = new ArrayList<>();

        if (feed.nearEarthObjects != null) {
            for (Map.Entry<String, List<FeedResponse.Neo>> entry : feed.nearEarthObjects.entrySet()) {
                String date = entry.getKey();
                for (FeedResponse.Neo neo : entry.getValue()) {
                    csvData.add(new CsvNeoData(
                            neo.id,
                            neo.name,
                            neo.absoluteMagnitudeH,
                            (neo.estimatedDiameter != null && neo.estimatedDiameter.meters != null)
                                    ? Double.parseDouble(DF.format(neo.estimatedDiameter.meters.min))
                                    : null,
                            (neo.estimatedDiameter != null && neo.estimatedDiameter.meters != null)
                                    ? Double.parseDouble(DF.format(neo.estimatedDiameter.meters.max))
                                    : null,
                            neo.hazardous,
                            (neo.closeApproachData != null && !neo.closeApproachData.isEmpty())
                                    ? neo.closeApproachData.get(0).approachDateFull
                                    : null,
                            (neo.closeApproachData != null && !neo.closeApproachData.isEmpty()
                                    && neo.closeApproachData.get(0).relativeVelocity != null)
                                            ? Double.parseDouble(
                                                    DF.format(Double.parseDouble(
                                                            neo.closeApproachData.get(0).relativeVelocity.kmPerSec)))
                                            : null,
                            (neo.closeApproachData != null && !neo.closeApproachData.isEmpty())
                                    ? neo.closeApproachData.get(0).orbitingBody
                                    : null,
                            String.valueOf(date)));
                }
            }
        }

        // Criar CSV
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(CsvNeoData.class).withHeader();
        String csvContent = csvMapper.writer(schema).writeValueAsString(csvData);

        // Salvar no MinIO
        String key = "raw/" + data.toString() + "/neows-feed-" + System.currentTimeMillis() + ".csv";
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("text/csv")
                        .build(),
                RequestBody.fromString(csvContent, StandardCharsets.UTF_8));

        Log.info("Arquivo CSV salvo no MinIO com key: " + key);
        return key;
    }

    // Classe interna para representar os dados em formato flat para CSV
    public static class CsvNeoData {
        public String neoId;
        public String nome;
        public Double magnitudeAbsoluta;
        public Double diametroMinM;
        public Double diametroMaxM;
        public boolean ehPotencialmentePerigoso;
        public String dataPrimeiraAproximacao;
        public Double velocidadeKmS;
        public String planetaAlvo;
        public String criadoEm;

        public CsvNeoData() {
        }

        public CsvNeoData(

                String neoId,
                String nome,
                Double magnitudeAbsoluta,
                Double diametroMinM,
                Double diametroMaxM,
                boolean ehPotencialmentePerigoso,
                String dataPrimeiraAproximacao,
                Double velocidadeKmS,
                String planetaAlvo,
                String criadoEm) {
            this.neoId = neoId;
            this.nome = nome;
            this.magnitudeAbsoluta = magnitudeAbsoluta;
            this.diametroMinM = diametroMinM;
            this.diametroMaxM = diametroMaxM;
            this.ehPotencialmentePerigoso = ehPotencialmentePerigoso;
            this.dataPrimeiraAproximacao = dataPrimeiraAproximacao;
            this.velocidadeKmS = velocidadeKmS;
            this.planetaAlvo = planetaAlvo;
            this.criadoEm = criadoEm;

        }

    }
}