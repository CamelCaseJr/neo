package org.acme.domain.dtos;
import java.net.URI;

import org.acme.domain.models.NeoObject;

import io.quarkus.arc.All;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NeoObjectResponse {

    private Long id;
    private String neoId;
    private String nome;
    private Double magnitudeAbsoluta;
    private Double diametroMinM;
    private Double diametroMaxM;
    private boolean ehPotencialmentePerigoso;
    private String planetaAlvo;
    private URI self;

    // Construtor vazio (necessário para Jackson)
    public NeoObjectResponse() {
    }

    // Construtor que recebe o NeoObject e a URI
    public NeoObjectResponse(NeoObject neo, URI self) {
        this.id = neo.id;
        this.neoId = neo.neoId;
        this.nome = neo.nome;
        this.magnitudeAbsoluta = neo.magnitudeAbsoluta;
        this.diametroMinM = neo.diametroMinM;
        this.diametroMaxM = neo.diametroMaxM;
        this.ehPotencialmentePerigoso = neo.ehPotencialmentePerigoso;
        this.planetaAlvo = neo.planetaAlvo;
        this.self = self;
    }



}