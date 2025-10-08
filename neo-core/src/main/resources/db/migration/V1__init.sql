CREATE TABLE IF NOT EXISTS neo_object (
  id BIGSERIAL PRIMARY KEY,
  neo_id VARCHAR(40) UNIQUE NOT NULL,
  nome VARCHAR(200) NOT NULL,
  magnitude_absoluta DOUBLE PRECISION,
  diametro_min_m DOUBLE PRECISION,
  diametro_max_m DOUBLE PRECISION,
  eh_potencialmente_perigoso BOOLEAN NOT NULL,
  data_primeira_aproximacao TIMESTAMP NULL,
  velocidade_km_s DOUBLE PRECISION NULL,
  planeta_alvo VARCHAR(64) NULL,
  origem_json_s3_key VARCHAR(512) NULL,
  criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);
