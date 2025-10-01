CREATE TABLE IF NOT EXISTS neo_object (
  id SERIAL PRIMARY KEY,
  neo_id VARCHAR(40) UNIQUE NOT NULL,
  nome VARCHAR(200) NOT NULL,
  magnitude_absoluta NUMERIC,
  diametro_min_m NUMERIC,
  diametro_max_m NUMERIC,
  eh_potencialmente_perigoso BOOLEAN NOT NULL,
  data_primeira_aproximacao TIMESTAMP NULL,
  velocidade_km_s NUMERIC NULL,
  planeta_alvo VARCHAR(64) NULL,
  origem_json_s3_key VARCHAR(512) NULL,
  criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);
