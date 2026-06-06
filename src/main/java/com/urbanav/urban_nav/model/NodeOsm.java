package com.urbanav.urban_nav.model;

public class NodeOsm {

    private long id;
    private double lat;
    private double lon;
    private String nome;          // nome da rua/POI se existir
    private boolean visitado;
    private int indice;           // posicao no grafo (para Dijkstra)

    public NodeOsm(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.visitado = false;
    }

    // Calcula distancia em metros entre dois nos (formula Haversine)
    public double distanciaAte(NodeOsm outro) {
        final double R = 6371000; // raio da Terra em metros
        double dLat = Math.toRadians(outro.lat - this.lat);
        double dLon = Math.toRadians(outro.lon - this.lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(outro.lat))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public String toString() {
        return "NodeOsm{id=" + id + ", lat=" + lat + ", lon=" + lon
                + (nome != null ? ", nome='" + nome + "'" : "") + "}";
    }

    // Getters e Setters
    public long getId()              { return id; }
    public double getLat()           { return lat; }
    public double getLon()           { return lon; }
    public String getNome()          { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public boolean isVisitado()      { return visitado; }
    public void setVisitado(boolean visitado) { this.visitado = visitado; }
    public int getIndice()           { return indice; }
    public void setIndice(int indice){ this.indice = indice; }
}
