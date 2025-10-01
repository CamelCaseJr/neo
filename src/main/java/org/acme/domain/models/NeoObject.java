package org.acme.domain.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "neo_object")
public class NeoObject extends PanacheEntityBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name="neo_id", unique = true, nullable = false, length = 40)
    public String neoId;

    @Column(name="nome", nullable = false, length = 200)
    public String nome;

    @Column(name="magnitude_absoluta")
    public Double magnitudeAbsoluta;

    @Column(name="diametro_min_m")
    public Double diametroMinM;

    @Column(name="diametro_max_m")
    public Double diametroMaxM;

    @Column(name="eh_potencialmente_perigoso", nullable = false)
    public boolean ehPotencialmentePerigoso;

    @Column(name="data_primeira_aproximacao")
    public OffsetDateTime dataPrimeiraAproximacao;

    @Column(name="velocidade_km_s")
    public Double velocidadeKmS;

    @Column(name="planeta_alvo", length = 64)
    public String planetaAlvo;

    @Column(name="origem_json_s3_key", length=512)
    public String origemJsonS3Key;

    @Column(name="criado_em", nullable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();
}
