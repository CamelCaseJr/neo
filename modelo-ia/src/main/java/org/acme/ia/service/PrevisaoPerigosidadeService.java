package org.acme.ia.service;

import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;

@ApplicationScoped
public class PrevisaoPerigosidadeService {

    /**
     * Exemplo de método para prever se um NEO é perigoso
     * usando características como magnitude, diâmetro e velocidade
     */
    public boolean preverPerigosidade(
            Double magnitudeAbsoluta, 
            Double diametroMaxM, 
            Double velocidadeKmS) {
        
        Log.infof("Prevendo perigosidade: mag=%s, diam=%s, vel=%s", 
            magnitudeAbsoluta, diametroMaxM, velocidadeKmS);
        
        // Lógica simples de exemplo (substituir por modelo real de ML)
        // NEOs com magnitude baixa (mais brilhantes), grandes e rápidos são mais perigosos
        if (magnitudeAbsoluta == null || diametroMaxM == null || velocidadeKmS == null) {
            return false;
        }
        
        // Critérios simplificados
        boolean magnitudePerigosa = magnitudeAbsoluta < 22.0;
        boolean tamanhoPerigoso = diametroMaxM > 140.0; // > 140m
        boolean velocidadePerigosa = velocidadeKmS > 15.0;
        
        int criteriosAtendidos = 0;
        if (magnitudePerigosa) criteriosAtendidos++;
        if (tamanhoPerigoso) criteriosAtendidos++;
        if (velocidadePerigosa) criteriosAtendidos++;
        
        // Se atender 2 ou mais critérios, considera perigoso
        return criteriosAtendidos >= 2;
    }

    /**
     * Calcula score de risco (0-100)
     */
    public double calcularScoreRisco(
            Double magnitudeAbsoluta, 
            Double diametroMaxM, 
            Double velocidadeKmS) {
        
        if (magnitudeAbsoluta == null || diametroMaxM == null || velocidadeKmS == null) {
            return 0.0;
        }
        
        // Normalização e ponderação simplificada
        double scoreMagnitude = Math.max(0, (22.0 - magnitudeAbsoluta) / 22.0) * 30;
        double scoreTamanho = Math.min(diametroMaxM / 1000.0, 1.0) * 40;
        double scoreVelocidade = Math.min(velocidadeKmS / 30.0, 1.0) * 30;
        
        double scoreTotal = scoreMagnitude + scoreTamanho + scoreVelocidade;
        
        return Math.min(scoreTotal, 100.0);
    }
}
