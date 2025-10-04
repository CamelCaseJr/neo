package org.acme.ia.dto;

public class PrevisaoResponse {
    public boolean ehPerigoso;
    public double scoreRisco;
    public String justificativa;

    public PrevisaoResponse() {
    }

    public PrevisaoResponse(boolean ehPerigoso, double scoreRisco, String justificativa) {
        this.ehPerigoso = ehPerigoso;
        this.scoreRisco = scoreRisco;
        this.justificativa = justificativa;
    }
}
