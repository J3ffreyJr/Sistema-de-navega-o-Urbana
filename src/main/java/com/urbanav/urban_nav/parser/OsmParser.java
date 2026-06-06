package com.urbanav.urban_nav.parser;

import com.urbanav.urban_nav.graph.Grafo;
import com.urbanav.urban_nav.model.NodeOsm;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class OsmParser {

    // Tipos de via que interessam para navegacao
    private static final java.util.Set<String> HIGHWAY_VALIDOS = java.util.Set.of(
            "motorway", "trunk", "primary", "secondary", "tertiary",
            "unclassified", "residential", "service",
            "motorway_link", "trunk_link", "primary_link", "secondary_link"
    );

    public Grafo parse() throws Exception {
        System.out.println("A carregar OSM: " + "data/maputo-centro.osm");

        Grafo grafo = new Grafo();

        // Fase 1: Recolher todos os nos com lat/lon
        java.util.Map<Long, NodeOsm> todosNos = new java.util.HashMap<>();

        // Fase 2: Recolher ways de highway e seus nd refs
        List<WayTemp> ways = new ArrayList<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        InputStream fis = getClass()
        .getClassLoader()
        .getResourceAsStream("data/maputo-centro.osm");

if (fis == null) {
    throw new FileNotFoundException(
            "data/maputo-centro.osm não encontrado no classpath");
}

try (fis) {

    XMLStreamReader reader = factory.createXMLStreamReader(fis);

    WayTemp wayAtual = null;
    boolean dentroDeWay = false;
    String nomeRuaAtual = null;
    boolean highwayValido = false;
   
            while (reader.hasNext()) {
                int evento = reader.next();

                if (evento == XMLStreamConstants.START_ELEMENT) {
                    String tag = reader.getLocalName();

                    if (tag.equals("node")) {
                        long id  = Long.parseLong(reader.getAttributeValue(null, "id"));
                        double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));
                        double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));
                        todosNos.put(id, new NodeOsm(id, lat, lon));

                    } else if (tag.equals("way")) {
                        dentroDeWay = true;
                        wayAtual = new WayTemp();
                        nomeRuaAtual = null;
                        highwayValido = false;

                    } else if (dentroDeWay && tag.equals("nd")) {
                        long ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                        wayAtual.refs.add(ref);

                    } else if (dentroDeWay && tag.equals("tag")) {
                        String k = reader.getAttributeValue(null, "k");
                        String v = reader.getAttributeValue(null, "v");
                        if ("highway".equals(k) && HIGHWAY_VALIDOS.contains(v)) {
                            highwayValido = true;
                        }
                        if ("name".equals(k)) {
                            nomeRuaAtual = v;
                        }
                    }

                } else if (evento == XMLStreamConstants.END_ELEMENT) {
                    String tag = reader.getLocalName();
                    if (tag.equals("way") && dentroDeWay) {
                        if (highwayValido && wayAtual.refs.size() >= 2) {
                            wayAtual.nomeRua = nomeRuaAtual;
                            ways.add(wayAtual);
                        }
                        dentroDeWay = false;
                        wayAtual = null;
                    }
                }
            }
            reader.close();
        }

        System.out.println("Nos OSM lidos: " + todosNos.size());
        System.out.println("Ways de highway encontradas: " + ways.size());

        // Fase 3: Adicionar ao grafo apenas nos referenciados por ways validas
        for (WayTemp way : ways) {
            for (long ref : way.refs) {
                NodeOsm n = todosNos.get(ref);
                if (n != null) grafo.adicionarNo(n);
            }
        }

        // Fase 4: Criar arestas entre nos consecutivos de cada way
        for (WayTemp way : ways) {
            for (int i = 0; i < way.refs.size() - 1; i++) {
                long idA = way.refs.get(i);
                long idB = way.refs.get(i + 1);
                grafo.adicionarAresta(idA, idB, way.nomeRua);
            }
        }

        System.out.println("Grafo construido: " + grafo.totalNos()
                + " nos, " + grafo.getTotalArestas() + " arestas");
        return grafo;
    }

    // Classe auxiliar temporaria durante o parse
    private static class WayTemp {
        List<Long> refs = new ArrayList<>();
        String nomeRua;
    }
}
