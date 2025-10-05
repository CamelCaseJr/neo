package org.acme.ia.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.logging.Log;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.CSVLoader;

@ApplicationScoped
public class MLTrainingService {

    @Inject
    S3Client s3;
    @ConfigProperty(name = "neo.minio.bucket")
    String bucket;

    // As colunas do seu CSV (iguais às que você gera no MinIO)
    private static final String[] FEATURE_COLUMNS = new String[] {
            "magnitudeAbsoluta", "diametroMinM", "diametroMaxM", "velocidadeKmS"
    };
    private static final String RESPONSE_COLUMN = "ehPotencialmentePerigoso"; // "true"/"false"

    /**
     * Treina com todos os CSVs disponíveis no bucket
     */
    public TrainingResult treinarComTodosBuckets() throws Exception {
        Log.info("Treino (WEKA): usando TODOS os CSVs do bucket");

        List<S3Object> csvs = listarTodosOsCsvs();
        if (csvs.isEmpty())
            throw new IllegalStateException("Nenhum CSV encontrado no bucket.");

        return executarTreinamento(csvs);
    }

    /**
     * Treina com CSVs de um período específico (método original)
     */
    public TrainingResult treinar(LocalDate start, LocalDate end) throws Exception {
        Log.infof("Treino (WEKA): %s a %s", start, end);

        List<S3Object> csvs = listarCsvsNoIntervalo(start, end);
        if (csvs.isEmpty())
            throw new IllegalStateException("Nenhum CSV no período.");

        return executarTreinamento(csvs);
    }

    /**
     * Executa o treinamento com a lista de CSVs fornecida
     */
    private TrainingResult executarTreinamento(List<S3Object> csvs) throws Exception {

        // -------- 1) Consolidar todos os CSVs em um único arquivo temporário --------
        Path consolidadoCsv = Files.createTempFile("neows-consolidated-", ".csv");
        boolean primeiraLinha = true;
        int totalLinhas = 0;
        
        try (var writer = Files.newBufferedWriter(consolidadoCsv)) {
            for (S3Object obj : csvs) {
                Log.info("Lendo: " + obj.key());
                
                try (InputStream is = s3.getObject(GetObjectRequest.builder()
                        .bucket(bucket).key(obj.key()).build());
                     var reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
                    
                    String linha;
                    boolean primeiraLinhaArquivo = true;
                    
                    while ((linha = reader.readLine()) != null) {
                        // Escreve o cabeçalho apenas uma vez
                        if (primeiraLinhaArquivo) {
                            if (primeiraLinha) {
                                writer.write(linha);
                                writer.newLine();
                                primeiraLinha = false;
                            }
                            primeiraLinhaArquivo = false;
                        } else {
                            // Escreve as linhas de dados
                            writer.write(linha);
                            writer.newLine();
                            totalLinhas++;
                        }
                    }
                }
                Log.infof("CSV processado: %s", obj.key());
            }
        }
        
        Log.infof("Arquivo consolidado criado com %d linhas de dados", totalLinhas);
        
        // -------- 2) Carregar o arquivo consolidado com Weka --------
        CSVLoader loader = new CSVLoader();
        loader.setSource(consolidadoCsv.toFile());
        Instances all = loader.getDataSet();
        
        // Limpa o arquivo temporário
        Files.deleteIfExists(consolidadoCsv);
        
        if (all == null || all.isEmpty())
            throw new IllegalStateException("Dataset ficou vazio.");
            
        // Define o índice da classe
        int classIndex = all.attribute(RESPONSE_COLUMN).index();
        all.setClassIndex(classIndex);
        
        Log.infof("Dataset carregado: %d instâncias, %d atributos, classe no índice %d", 
            all.numInstances(), all.numAttributes(), all.classIndex());
        if (all.classIndex() < 0)
            all.setClassIndex(all.attribute(RESPONSE_COLUMN).index());

        // -------- 2) Split train/test 80/20 --------
        all.randomize(new java.util.Random(123L));
        int trainSize = (int) Math.round(all.numInstances() * 0.7);
        Instances train = new Instances(all, 0, trainSize);
        Instances test = new Instances(all, trainSize, all.numInstances() - trainSize);

        Log.infof("Train=%d, Test=%d", train.numInstances(), test.numInstances());

        // -------- 3) Treinar RandomForest (WEKA) --------
        RandomForest rf = new RandomForest();
        rf.setNumIterations(100); // número de árvores
        rf.setSeed(123);
        // rf.setMaxDepth(0); // 0 = ilimitado (default)
        // rf.setNumFeatures(0); // 0 = sqrt(numAttributes) padrão
        rf.buildClassifier(train);

        // -------- 4) Avaliação com Matriz de Confusão, Precisão, Recall, F1 e AUC --------
        Evaluation wekaEval = new Evaluation(train);
        wekaEval.evaluateModel(rf, test);

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Avaliação Holdout (30%) ===\n");
        sb.append(wekaEval.toSummaryString()).append('\n');
        sb.append(wekaEval.toClassDetailsString()).append('\n');
        sb.append(wekaEval.toMatrixString()).append('\n');

        // Adicionar AUC para classe "true" (potencialmente perigoso)
        int idxTrue = test.classAttribute().indexOfValue("true");
        if (idxTrue >= 0) {
            double auc = wekaEval.areaUnderROC(idxTrue);
            sb.append(String.format("\nAUC (classe positiva='true'): %.4f\n", auc));
            Log.infof("AUC (potencialmente perigoso=true): %.4f", auc);
        }

        String eval = sb.toString();
        Log.info(eval);

        // -------- 5) Salvar modelo + header --------
        // Dica: salve também o "header" (estrutura dos atributos) para facilitar a
        // predição
        Path tmpModel = Files.createTempFile("neows-weka-", ".model");
        Path tmpHeader = Files.createTempFile("neows-weka-", ".header");

        SerializationHelper.write(tmpModel.toString(), rf);
        // O header é um Instances sem dados (0 instâncias) com o mesmo esquema
        Instances header = new Instances(train, 0);
        SerializationHelper.write(tmpHeader.toString(), header);

        String ts = String.valueOf(System.currentTimeMillis());
        String modelKey = "models/weka-rf-" + ts + ".model";
        String headerKey = "models/weka-rf-" + ts + ".header";

        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(modelKey)
                .contentType("application/octet-stream").build(),
                RequestBody.fromFile(tmpModel));
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(headerKey)
                .contentType("application/octet-stream").build(),
                RequestBody.fromFile(tmpHeader));

        Files.deleteIfExists(tmpModel);
        Files.deleteIfExists(tmpHeader);

        Log.info("Modelo salvo: " + modelKey + " (header: " + headerKey + ")");
        return new TrainingResult(eval, modelKey);
    }

    private List<S3Object> listarCsvsNoIntervalo(LocalDate start, LocalDate end) {
        return start.datesUntil(end.plusDays(1))
                .flatMap(date -> {
                    String prefix = "raw/" + date + "/";
                    ListObjectsV2Response resp = s3.listObjectsV2(
                            ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build());
                    return resp.contents().stream().filter(o -> o.key().endsWith(".csv"));
                })
                .sorted(Comparator.comparing(S3Object::key))
                .collect(Collectors.toList());
    }

    /**
     * Lista TODOS os CSVs disponíveis no bucket
     */
    private List<S3Object> listarTodosOsCsvs() {
        Log.info("Listando todos os CSVs no bucket: " + bucket);

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix("raw/")
                .build();

        ListObjectsV2Response response = s3.listObjectsV2(request);

        List<S3Object> csvs = response.contents().stream()
                .filter(o -> o.key().endsWith(".csv"))
                .sorted(Comparator.comparing(S3Object::key))
                .collect(Collectors.toList());

        Log.infof("Encontrados %d CSVs no bucket", csvs.size());
        return csvs;
    }

    public static class TrainingResult {
        public String evaluation;
        public String modelKey;

        public TrainingResult() {
        }

        public TrainingResult(String evaluation, String modelKey) {
            this.evaluation = evaluation;
            this.modelKey = modelKey;
        }
    }
}
