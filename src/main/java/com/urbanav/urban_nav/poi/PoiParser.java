package com.urbanav.urban_nav.poi;

import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.util.*;

/**
 * Extrai POIs directamente do ficheiro .osm local.
 * Lê nodes E ways com tags de nome + categoria reconhecida.
 * Para ways usa o centróide dos seus nós como coordenada do POI.
 * Não depende de nenhuma API externa.
 */
@Component
public class PoiParser {

    // Tags OSM que identificam um POI
    private static final Set<String> CATS = Set.of(
        "amenity", "shop", "tourism", "leisure", "office",
        "healthcare", "historic", "natural", "building"
    );

    // Mapeamento de tipo OSM → categoria legível
    private static final Map<String, String> TIPO_LABEL = new HashMap<>();
    static {
        // amenity
        TIPO_LABEL.put("restaurant","Restaurante"); TIPO_LABEL.put("fast_food","Fast Food");
        TIPO_LABEL.put("cafe","Café"); TIPO_LABEL.put("bar","Bar"); TIPO_LABEL.put("pub","Pub");
        TIPO_LABEL.put("hospital","Hospital"); TIPO_LABEL.put("clinic","Clínica");
        TIPO_LABEL.put("pharmacy","Farmácia"); TIPO_LABEL.put("doctors","Médico");
        TIPO_LABEL.put("school","Escola"); TIPO_LABEL.put("university","Universidade");
        TIPO_LABEL.put("college","Instituto"); TIPO_LABEL.put("kindergarten","Jardim de Infância");
        TIPO_LABEL.put("bank","Banco"); TIPO_LABEL.put("atm","Multibanco");
        TIPO_LABEL.put("fuel","Posto de Combustível"); TIPO_LABEL.put("parking","Estacionamento");
        TIPO_LABEL.put("police","Polícia"); TIPO_LABEL.put("fire_station","Bombeiros");
        TIPO_LABEL.put("post_office","Correios"); TIPO_LABEL.put("library","Biblioteca");
        TIPO_LABEL.put("place_of_worship","Local de Culto"); TIPO_LABEL.put("cinema","Cinema");
        TIPO_LABEL.put("theatre","Teatro"); TIPO_LABEL.put("marketplace","Mercado");
        TIPO_LABEL.put("bus_station","Terminal de Autocarros"); TIPO_LABEL.put("ferry_terminal","Terminal de Ferry");
        TIPO_LABEL.put("townhall","Câmara Municipal"); TIPO_LABEL.put("embassy","Embaixada");
        TIPO_LABEL.put("community_centre","Centro Comunitário");
        // shop
        TIPO_LABEL.put("supermarket","Supermercado"); TIPO_LABEL.put("mall","Centro Comercial");
        TIPO_LABEL.put("department_store","Grande Superfície"); TIPO_LABEL.put("convenience","Mercearia");
        TIPO_LABEL.put("clothes","Loja de Roupa"); TIPO_LABEL.put("electronics","Electrónica");
        TIPO_LABEL.put("hardware","Ferragens"); TIPO_LABEL.put("bakery","Padaria");
        TIPO_LABEL.put("butcher","Talho"); TIPO_LABEL.put("greengrocer","Frutaria");
        TIPO_LABEL.put("hairdresser","Cabeleireiro"); TIPO_LABEL.put("beauty","Beleza");
        TIPO_LABEL.put("mobile_phone","Telemóveis"); TIPO_LABEL.put("optician","Óptica");
        TIPO_LABEL.put("car","Automóveis"); TIPO_LABEL.put("bicycle","Bicicletas");
        TIPO_LABEL.put("shoes","Sapataria"); TIPO_LABEL.put("books","Livraria");
        TIPO_LABEL.put("florist","Florista"); TIPO_LABEL.put("jewelry","Joalharia");
        // tourism
        TIPO_LABEL.put("hotel","Hotel"); TIPO_LABEL.put("hostel","Hostel");
        TIPO_LABEL.put("guest_house","Pensão"); TIPO_LABEL.put("museum","Museu");
        TIPO_LABEL.put("attraction","Atracção"); TIPO_LABEL.put("viewpoint","Miradouro");
        TIPO_LABEL.put("information","Informação Turística");
        // leisure
        TIPO_LABEL.put("park","Parque"); TIPO_LABEL.put("stadium","Estádio");
        TIPO_LABEL.put("sports_centre","Centro Desportivo"); TIPO_LABEL.put("swimming_pool","Piscina");
        TIPO_LABEL.put("fitness_centre","Ginásio"); TIPO_LABEL.put("golf_course","Campo de Golf");
        TIPO_LABEL.put("beach","Praia"); TIPO_LABEL.put("marina","Marina");
        // office / building
        TIPO_LABEL.put("government","Governo"); TIPO_LABEL.put("company","Empresa");
        TIPO_LABEL.put("ngo","ONG"); TIPO_LABEL.put("commercial","Edifício Comercial");
        // historic
        TIPO_LABEL.put("monument","Monumento"); TIPO_LABEL.put("ruins","Ruínas");
        TIPO_LABEL.put("fort","Forte"); TIPO_LABEL.put("memorial","Memorial");
        // transport
        TIPO_LABEL.put("aerodrome","Aeroporto"); TIPO_LABEL.put("train_station","Estação de Comboio");
    }

    public List<Poi> parse(String caminhoFicheiro) throws Exception {
        System.out.println("=== PoiParser: a extrair POIs de " + caminhoFicheiro + " ===");

        // Estruturas temporárias
        Map<Long, double[]> coordsNos = new HashMap<>();  // id → [lat, lon]
        List<Poi> pois = new ArrayList<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        try (FileInputStream fis = new FileInputStream(caminhoFicheiro)) {
            XMLStreamReader reader = factory.createXMLStreamReader(fis);

            // Estado do parser
            String contexto = null; // "node" ou "way"
            long idAtual = -1;
            double latAtual = 0, lonAtual = 0;
            Map<String, String> tagsAtual = new HashMap<>();
            List<Long> ndRefs = new ArrayList<>();

            while (reader.hasNext()) {
                int ev = reader.next();

                if (ev == XMLStreamConstants.START_ELEMENT) {
                    String el = reader.getLocalName();

                    if (el.equals("node")) {
                        contexto = "node";
                        idAtual = Long.parseLong(reader.getAttributeValue(null, "id"));
                        String latStr = reader.getAttributeValue(null, "lat");
                        String lonStr = reader.getAttributeValue(null, "lon");
                        if (latStr != null && lonStr != null) {
                            latAtual = Double.parseDouble(latStr);
                            lonAtual = Double.parseDouble(lonStr);
                            coordsNos.put(idAtual, new double[]{latAtual, lonAtual});
                        }
                        tagsAtual = new HashMap<>();

                    } else if (el.equals("way")) {
                        contexto = "way";
                        idAtual = Long.parseLong(reader.getAttributeValue(null, "id"));
                        tagsAtual = new HashMap<>();
                        ndRefs = new ArrayList<>();

                    } else if ("way".equals(contexto) && el.equals("nd")) {
                        String ref = reader.getAttributeValue(null, "ref");
                        if (ref != null) ndRefs.add(Long.parseLong(ref));

                    } else if (el.equals("tag")) {
                        String k = reader.getAttributeValue(null, "k");
                        String v = reader.getAttributeValue(null, "v");
                        if (k != null && v != null) tagsAtual.put(k, v);
                    }

                } else if (ev == XMLStreamConstants.END_ELEMENT) {
                    String el = reader.getLocalName();

                    if ((el.equals("node") && "node".equals(contexto)) ||
                        (el.equals("way")  && "way".equals(contexto))) {

                        // Verificar se é um POI com nome
                        String nome = tagsAtual.get("name");
                        if (nome != null && !nome.isBlank()) {

                            // Encontrar categoria e tipo
                            String cat = null, tipo = null;
                            for (String c : CATS) {
                                if (tagsAtual.containsKey(c)) {
                                    cat = c;
                                    tipo = tagsAtual.get(c);
                                    break;
                                }
                            }

                            // Aceitar também nodes/ways com nome mas sem categoria explícita
                            // desde que tenham outros indicadores de POI
                            boolean temIndicador = cat != null
                                || tagsAtual.containsKey("brand")
                                || tagsAtual.containsKey("operator")
                                || "yes".equals(tagsAtual.get("building"));

                            if (temIndicador) {
                                double finalLat = latAtual, finalLon = lonAtual;

                                // Para ways: calcular centróide dos nós
                                if ("way".equals(el) && !ndRefs.isEmpty()) {
                                    double sumLat = 0, sumLon = 0;
                                    int count = 0;
                                    for (long ref : ndRefs) {
                                        double[] c = coordsNos.get(ref);
                                        if (c != null) { sumLat += c[0]; sumLon += c[1]; count++; }
                                    }
                                    if (count > 0) { finalLat = sumLat / count; finalLon = sumLon / count; }
                                    else continue; // sem coords
                                }

                                if (finalLat == 0 && finalLon == 0) { contexto = null; continue; }

                                String catFinal = cat != null ? cat : "place";
                                String tipoFinal = tipo != null ? tipo : "place";

                                Poi poi = new Poi(idAtual, nome, finalLat, finalLon, catFinal, tipoFinal);

                                // Morada
                                String rua = tagsAtual.get("addr:street");
                                String num = tagsAtual.get("addr:housenumber");
                                String bairro = tagsAtual.get("addr:suburb");
                                if (rua != null) {
                                    poi.setMorada(rua + (num != null ? " " + num : "") + (bairro != null ? ", " + bairro : ""));
                                } else if (bairro != null) {
                                    poi.setMorada(bairro);
                                }

                                poi.setTelefone(tagsAtual.get("phone"));
                                poi.setWebsite(tagsAtual.get("website"));
                                poi.setHorario(tagsAtual.get("opening_hours"));

                                pois.add(poi);
                            }
                        }
                        contexto = null;
                        tagsAtual = new HashMap<>();
                        ndRefs = new ArrayList<>();
                    }
                }
            }
            reader.close();
        }

        System.out.println("=== PoiParser: " + pois.size() + " POIs extraídos ===");
        return pois;
    }

    public String getLabelTipo(String tipo) {
        return TIPO_LABEL.getOrDefault(tipo, tipo);
    }
}