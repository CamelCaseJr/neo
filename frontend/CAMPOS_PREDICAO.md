# 🔮 Campos de Predição - Machine Learning

## Campos Utilizados na Predição

O modelo de Machine Learning utiliza **4 campos** para prever se um NEO (Near Earth Object) é potencialmente perigoso:

### 1️⃣ **Magnitude Absoluta (H)**

- **Campo**: `magnitudeAbsoluta`
- **Tipo**: `Double`
- **Unidade**: Magnitude (escala logarítmica)
- **Exemplo**: `20.5`
- **Descrição**: Quanto **menor** o valor, **maior** o objeto. É uma medida do brilho intrínseco do asteroide.

### 2️⃣ **Diâmetro Mínimo**

- **Campo**: `diametroMinM`
- **Tipo**: `Double`
- **Unidade**: Metros (m)
- **Exemplo**: `100.5`
- **Descrição**: Estimativa do diâmetro mínimo do NEO em metros.

### 3️⃣ **Diâmetro Máximo**

- **Campo**: `diametroMaxM`
- **Tipo**: `Double`
- **Unidade**: Metros (m)
- **Exemplo**: `250.8`
- **Descrição**: Estimativa do diâmetro máximo do NEO em metros.

### 4️⃣ **Velocidade Relativa**

- **Campo**: `velocidadeKmS`
- **Tipo**: `Double`
- **Unidade**: Quilômetros por segundo (km/s)
- **Exemplo**: `13.9`
- **Descrição**: Velocidade relativa do NEO em relação à Terra.

---

## 📝 Exemplo de Payload

```json
{
  "magnitudeAbsoluta": 20.5,
  "diametroMinM": 100.5,
  "diametroMaxM": 250.8,
  "velocidadeKmS": 13.9
}
```

---

## ✅ Resposta da API

```json
{
  "preditoPerigoso": true,
  "probabilidadePerigoso": 0.85
}
```

### Campos da Resposta:

- **`preditoPerigoso`**: `boolean` - `true` se o NEO é classificado como perigoso
- **`probabilidadePerigoso`**: `double` - Probabilidade (0.0 a 1.0) de o NEO ser perigoso

---

## 🎯 Threshold de Decisão

O modelo usa um **threshold (TAU)** para decidir se um NEO é perigoso:

- Se `probabilidadePerigoso >= TAU` → **PERIGOSO** ✅
- Se `probabilidadePerigoso < TAU` → **SEGURO** ❌

---

## 🚨 Observações Importantes

1. **Magnitude Absoluta** é inversamente proporcional ao tamanho (menor valor = maior objeto)
2. **Velocidade** deve ser em **km/s**, não km/h (ex: 50.000 km/h = 13.9 km/s)
3. **Diâmetros** devem ser em **metros**, não quilômetros
4. Todos os 4 campos são **obrigatórios** para a predição

---

## 🔄 Conversões Úteis

### Velocidade

- **km/h → km/s**: dividir por 3600
- **Exemplo**: 50.000 km/h ÷ 3600 = 13.9 km/s

### Diâmetro

- **km → m**: multiplicar por 1000
- **Exemplo**: 0.25 km × 1000 = 250 m

---

## 📊 Exemplos de NEOs

### NEO Perigoso

```json
{
  "magnitudeAbsoluta": 18.5,
  "diametroMinM": 500,
  "diametroMaxM": 1200,
  "velocidadeKmS": 25.0
}
```

Resultado esperado: **PERIGOSO** (alta probabilidade)

### NEO Seguro

```json
{
  "magnitudeAbsoluta": 25.0,
  "diametroMinM": 20,
  "diametroMaxM": 50,
  "velocidadeKmS": 8.0
}
```

Resultado esperado: **SEGURO** (baixa probabilidade)

---

## 🛠️ Testando no Frontend

1. Acesse a página **Machine Learning** (`http://localhost:3000/ml.html`)
2. Preencha os 4 campos obrigatórios
3. Clique em **"Fazer Predição"**
4. Veja o resultado com a classificação e confiança

---

## 📚 Referências

- **API Endpoint**: `POST http://localhost:8081/ml/predict`
- **Content-Type**: `application/json`
- **Método**: `POST`
