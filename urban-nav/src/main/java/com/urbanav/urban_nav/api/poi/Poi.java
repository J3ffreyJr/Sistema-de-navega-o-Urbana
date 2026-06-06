package com.urbanav.urban_nav.api.poi;

public class Poi {

    private long osmId;
    private String nome;
    private double lat;
    private double lon;
    private String categoria;   // shop, amenity, tourism, leisure, office...
    private String tipo;        // mall, restaurant, supermarket, hospital...
    private String morada;      // addr:street + addr:housenumber se existir
    private String telefone;
    private String website;
    private String horario;

    public Poi(long osmId, String nome, double lat, double lon, String categoria, String tipo) {
        this.osmId = osmId;
        this.nome = nome;
        this.lat = lat;
        this.lon = lon;
        this.categoria = categoria;
        this.tipo = tipo;
    }

    // Getters
    public long getOsmId()       { return osmId; }
    public String getNome()      { return nome; }
    public double getLat()       { return lat; }
    public double getLon()       { return lon; }
    public String getCategoria() { return categoria; }
    public String getTipo()      { return tipo; }
    public String getMorada()    { return morada; }
    public String getTelefone()  { return telefone; }
    public String getWebsite()   { return website; }
    public String getHorario()   { return horario; }

    // Setters
    public void setMorada(String m)   { this.morada = m; }
    public void setTelefone(String t) { this.telefone = t; }
    public void setWebsite(String w)  { this.website = w; }
    public void setHorario(String h)  { this.horario = h; }

    @Override
    public String toString() {
        return "Poi{nome='" + nome + "', lat=" + lat + ", lon=" + lon + ", tipo=" + tipo + "}";
    }
}