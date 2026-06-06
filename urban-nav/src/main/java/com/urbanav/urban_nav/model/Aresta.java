package com.urbanav.urban_nav.model;

public class Aresta {

    private NodeOsm destino;
    private double peso;       // distancia em metros
    private String nomeRua;

    public Aresta(NodeOsm destino, double peso, String nomeRua) {
        this.destino = destino;
        this.peso = peso;
        this.nomeRua = nomeRua != null ? nomeRua : "";
    }

    @Override
    public String toString() {
        return "Aresta{destino=" + destino.getId()
                + ", peso=" + String.format("%.1f", peso) + "m"
                + ", rua='" + nomeRua + "'}";
    }

    // Getters
    public NodeOsm getDestino()  { return destino; }
    public double getPeso()      { return peso; }
    public String getNomeRua()   { return nomeRua; }
}
