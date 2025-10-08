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
import java.util.*;
import java.util.stream.Collectors;

import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.CSVLoader;

@ApplicationScoped
public class MLTrainingService {

    @Inject
    S3Client s3;

    @ConfigProperty(name = "neo.minio.bucket")
    String bucket;

    // custos mais equilibrados (pode ajustar via application.properties)
    @ConfigProperty(name = "ml.cost.fn", defaultValue = "15.0")
    double COST_FN;

    @ConfigProperty(name = "ml.cost.fp", defaultValue = "3.0")
    double COST_FP;

    // coluna de classe no CSV
    private static final String RESPONSE_COLUMN = "ehPotencialmentePerigoso";

    public TrainingResult treinar(LocalDate start, LocalDate end) throws Exception {
        Log.infof("Treino (WEKA): %s a %s", start, end);
        List<S3Object> csvs = listarCsvsNoIntervalo(start, end);
        if (csvs.isEmpty())
            throw new IllegalStateException("Nenhum CSV no período.");
        return executarTreinamento(csvs);
    }

    public TrainingResult treinarComTodosBuckets() throws Exception {
        Log.info("Treino (WEKA): usando TODOS os CSVs do bucket");
        List<S3Object> csvs = listarTodosOsCsvs();
        if (csvs.isEmpty())
            throw new IllegalStateException("Nenhum CSV encontrado no bucket.");
        return executarTreinamento(csvs);
    }

    private TrainingResult executarTreinamento(List<S3Object> csvs) throws Exception {
        // 1️⃣ Consolidar todos os CSVs em um único arquivo
        Path consolidadoCsv = consolidarCsvs(csvs);

        // 2️⃣ Carregar o arquivo consolidado em um objeto Instances (WEKA)
        Instances all = carregarDataset(consolidadoCsv);

        // 3️⃣ Separar treino e teste (70/30)
        StratifiedSplit split = dividirTreinoTeste(all);
        Instances train = split.train;
        Instances test = split.test;

        // 4️⃣ Treinar modelo com RandomForest + CostSensitive
        CostSensitiveClassifier modeloTreinado = treinarModelo(train, all);

        // 5️⃣ Avaliar modelo com dados de teste
        String avaliacao = avaliarModelo(modeloTreinado, train, test);

        // 6️⃣ Salvar modelo e cabeçalho no MinIO
        String modelKey = salvarModelo(modeloTreinado, train);

        return new TrainingResult(avaliacao, modelKey);
    }

    private Path consolidarCsvs(List<S3Object> csvs) throws Exception {
        Path consolidado = Files.createTempFile("neows-consolidated-", ".csv");
        boolean primeiraLinha = true;

        try (var writer = Files.newBufferedWriter(consolidado)) {
            for (S3Object obj : csvs) {
                Log.info("Lendo: " + obj.key());
                try (InputStream is = s3.getObject(GetObjectRequest.builder()
                        .bucket(bucket).key(obj.key()).build());
                        var reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
                    String linha;
                    boolean primeiraLinhaArquivo = true;

                    while ((linha = reader.readLine()) != null) {
                        if (primeiraLinhaArquivo) {
                            if (primeiraLinha) {
                                writer.write(linha);
                                writer.newLine();
                                primeiraLinha = false;
                            }
                            primeiraLinhaArquivo = false;
                        } else {
                            writer.write(linha);
                            writer.newLine();
                        }
                    }
                }
            }
        }
        return consolidado;
    }

    private Instances carregarDataset(Path consolidadoCsv) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(consolidadoCsv.toFile());
        Instances all = loader.getDataSet();
        Files.deleteIfExists(consolidadoCsv);

        int classIndex = all.attribute(RESPONSE_COLUMN).index();
        all.setClassIndex(classIndex);

        Log.infof("Dataset: %d instâncias, %d atributos, classe=%s",
                all.numInstances(), all.numAttributes(), all.classAttribute().name());

        return all;
    }

    private StratifiedSplit dividirTreinoTeste(Instances all) throws Exception {
        long seed = 123L;
        double trainRatio = 0.70;
        return stratifiedHoldout(all, trainRatio, seed);
    }

    private CostSensitiveClassifier treinarModelo(Instances train, Instances all) throws Exception {
        RandomForest rf = new RandomForest();
        rf.setNumIterations(100);
        rf.setSeed(123);

        var clsAttr = all.classAttribute();
        int idxFalse = clsAttr.indexOfValue("false");
        int idxTrue = clsAttr.indexOfValue("true");

        CostMatrix cm = new CostMatrix(2);
        cm.setElement(idxTrue, idxFalse, COST_FN);
        cm.setElement(idxFalse, idxTrue, COST_FP);
        cm.setElement(idxFalse, idxFalse, 0.0);
        cm.setElement(idxTrue, idxTrue, 0.0);

        CostSensitiveClassifier csc = new CostSensitiveClassifier();
        csc.setClassifier(rf);
        csc.setCostMatrix(cm);
        csc.setMinimizeExpectedCost(true);
        csc.buildClassifier(train);

        return csc;
    }

    private String avaliarModelo(CostSensitiveClassifier csc, Instances train, Instances test) throws Exception {
        Evaluation eval = new Evaluation(train);
        eval.evaluateModel(csc, test);

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Avaliação Holdout (70/30) ===\n");
        sb.append(eval.toSummaryString()).append('\n');
        sb.append(eval.toClassDetailsString()).append('\n');
        sb.append(eval.toMatrixString()).append('\n');

        int idxTrueVal = test.classAttribute().indexOfValue("true");
        if (idxTrueVal >= 0) {
            double auc = eval.areaUnderROC(idxTrueVal);
            sb.append(String.format("\nAUC (classe positiva='true'): %.4f\n", auc));
        }

        Log.info(sb.toString());
        return sb.toString();
    }

    private String salvarModelo(CostSensitiveClassifier csc, Instances train) throws Exception {
        Path tmpModel = Files.createTempFile("neows-weka-", ".model");
        Path tmpHeader = Files.createTempFile("neows-weka-", ".header");

        SerializationHelper.write(tmpModel.toString(), csc);
        Instances header = new Instances(train, 0);
        SerializationHelper.write(tmpHeader.toString(), header);

        String ts = String.valueOf(System.currentTimeMillis());
        String modelKey = "models/weka-rf-csc-" + ts + ".model";
        String headerKey = "models/weka-rf-csc-" + ts + ".header";

        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(modelKey)
                .contentType("application/octet-stream").build(),
                RequestBody.fromFile(tmpModel));
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(headerKey)
                .contentType("application/octet-stream").build(),
                RequestBody.fromFile(tmpHeader));

        Files.deleteIfExists(tmpModel);
        Files.deleteIfExists(tmpHeader);
        return modelKey;
    }

    // ===== split estratificado 70/30 =====
    private StratifiedSplit stratifiedHoldout(Instances all, double trainRatio, long seed) throws Exception {
        if (!all.classAttribute().isNominal()) {
            throw new IllegalStateException("Classe precisa ser nominal (true/false).");
        }
        Random rnd = new Random(seed);

        // valores possíveis da classe, como String
        List<String> classValues = Collections.list(all.classAttribute().enumerateValues())
                .stream().map(Object::toString).collect(Collectors.toList());

        // mapa classe -> índices
        Map<String, List<Integer>> byClass = new LinkedHashMap<>();
        for (String v : classValues)
            byClass.put(v, new ArrayList<>());

        for (int i = 0; i < all.numInstances(); i++) {
            String v = all.instance(i).stringValue(all.classIndex());
            byClass.get(v).add(i);
        }

        Instances train = new Instances(all, 0);
        Instances test = new Instances(all, 0);

        for (var e : byClass.entrySet()) {
            List<Integer> idxs = e.getValue();
            Collections.shuffle(idxs, rnd);

            int nTrain = (int) Math.round(idxs.size() * trainRatio);
            var trainIdxs = idxs.subList(0, nTrain);
            var testIdxs = idxs.subList(nTrain, idxs.size());

            for (int id : trainIdxs)
                train.add(all.instance(id));
            for (int id : testIdxs)
                test.add(all.instance(id));
        }

        train.setClassIndex(all.classIndex());
        test.setClassIndex(all.classIndex());
        return new StratifiedSplit(train, test);
    }

    private static class StratifiedSplit {
        final Instances train, test;

        StratifiedSplit(Instances t, Instances s) {
            this.train = t;
            this.test = s;
        }
    }

    // ===== utilitários S3 =====
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

    private List<S3Object> listarTodosOsCsvs() {
        ListObjectsV2Response response = s3.listObjectsV2(
                ListObjectsV2Request.builder().bucket(bucket).prefix("raw/").build());
        return response.contents().stream()
                .filter(o -> o.key().endsWith(".csv"))
                .sorted(Comparator.comparing(S3Object::key))
                .collect(Collectors.toList());
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
