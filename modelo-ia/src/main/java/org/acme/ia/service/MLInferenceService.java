package org.acme.ia.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

@ApplicationScoped
public class MLInferenceService {

    @Inject S3Client s3;

    @ConfigProperty(name = "neo.minio.bucket")
    String bucket;

    // <<< (1) Threshold configurável. Se não tiver no application.properties, usa 0.80 por padrão.
    @ConfigProperty(name = "ml.threshold", defaultValue = "0.80")
    double TAU;

    private volatile Classifier model;
    private volatile Instances header; // estrutura de atributos

    @PostConstruct
    void init() {
        try {
            carregarModeloMaisRecente();
        } catch (Exception e) {
            Log.warn("Ainda sem modelo carregado. Treine em /ml/train.", e);
        }
    }

    public synchronized void carregarModeloMaisRecente() throws Exception {
        ListObjectsV2Response res = s3.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket).prefix("models/").build());

        var latestModel = res.contents().stream()
                .filter(o -> o.key().endsWith(".model"))
                .max(Comparator.comparing(S3Object::lastModified))
                .orElseThrow(() -> new IllegalStateException("Nenhum modelo .model em models/."));

        String base = latestModel.key().substring(0, latestModel.key().length() - ".model".length());
        String headerKey = base + ".header";

        Path tmpModel = Files.createTempFile("neows-weka-", ".model");
        try (InputStream is = s3.getObject(GetObjectRequest.builder()
                .bucket(bucket).key(latestModel.key()).build())) {
            Files.copy(is, tmpModel, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        Path tmpHeader = Files.createTempFile("neows-weka-", ".header");
        try (InputStream is = s3.getObject(GetObjectRequest.builder()
                .bucket(bucket).key(headerKey).build())) {
            Files.copy(is, tmpHeader, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        this.model  = (Classifier) SerializationHelper.read(tmpModel.toString());
        this.header = (Instances)  SerializationHelper.read(tmpHeader.toString());

        Files.deleteIfExists(tmpModel);
        Files.deleteIfExists(tmpHeader);

        Log.info("Modelo carregado: " + latestModel.key());
        Log.infof("Threshold (TAU) carregado da config: %.3f", TAU);
    }

    public PredictionResult predict(FeaturesInput in) throws Exception {
        if (model == null || header == null) {
            throw new IllegalStateException("Modelo não carregado. Treine/Carregue primeiro.");
        }

        // Monta a instância com o mesmo schema do header
        Instance inst = new DenseInstance(header.numAttributes());
        inst.setDataset(header);

        setIfExists(inst, "magnitudeAbsoluta", in.magnitudeAbsoluta);
        setIfExists(inst, "diametroMinM",      in.diametroMinM);
        setIfExists(inst, "diametroMaxM",      in.diametroMaxM);
        setIfExists(inst, "velocidadeKmS",     in.velocidadeKmS);

        // Em vez de aceitar o rótulo "seco", usamos a distribuição de probabilidades
        double[] dist = model.distributionForInstance(inst);
        int idxTrue = header.classAttribute().indexOfValue("true");
        double pTrue = (idxTrue >= 0 && idxTrue < dist.length) ? dist[idxTrue] : Double.NaN;

        // <<< (1) Decisão por threshold: se pTrue >= TAU, marcamos como perigoso ("true").
        boolean perigoso = !Double.isNaN(pTrue) && pTrue >= TAU;

        PredictionResult out = new PredictionResult();
        out.preditoPerigoso = perigoso;
        out.probabilidadePerigoso = pTrue;
        return out;
    }

    private void setIfExists(Instance inst, String attrName, Double value) {
        Attribute a = header.attribute(attrName);
        if (a != null) {
            if (value == null) inst.setMissing(a);
            else inst.setValue(a, value);
        }
    }

    // DTOs
    public static class FeaturesInput {
        public Double magnitudeAbsoluta;
        public Double diametroMinM;
        public Double diametroMaxM;
        public Double velocidadeKmS;
    }
    public static class PredictionResult {
        public boolean preditoPerigoso;
        public double probabilidadePerigoso;
    }
}
