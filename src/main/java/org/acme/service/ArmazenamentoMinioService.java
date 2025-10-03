package org.acme.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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
    @Inject S3Client s3Client;
    @ConfigProperty(name = "neo.minio.bucket") String bucket;

    public void criarBucket() {
        try{
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());

        }catch(S3Exception e){
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    public String salvarJsonBruto(String conteudoJson, LocalDate data){
        
        criarBucket();
        String key = "raw/" + data.toString() + "/neows-feed-"+ System.currentTimeMillis();
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

}
