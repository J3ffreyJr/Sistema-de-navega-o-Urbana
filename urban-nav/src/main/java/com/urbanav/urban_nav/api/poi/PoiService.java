package com.urbanav.urban_nav.api.poi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Carrega todos os POIs do ficheiro OSM e expõe métodos de busca.
 * Índice por nome normalizado para pesquisa rápida e tolerante a acentos.
 * Não usa nenhuma API externa — tudo local.
 */
@Service
public class PoiService {

    @Autowired
    private PoiParser poiParser;

    @Value("${osm.file.path:src/main/resources/data/maputo.osm}")
    private String osmFilePath;

    // Lista completa de POIs
    private List<Poi> todos = new ArrayList<>();

    // Índice: nome normalizado → lista de POIs
    private Map<String, List<Poi>> indiceNome = new HashMap<>();

    @PostConstruct
    public void inicializar() {
        try {
            todos = poiParser.parse(osmFilePath);
            construirIndice();
            System.out.println("PoiService: índice construído com " + todos.size() + " POIs");
        } catch (Exception e) {
            System.err.println("PoiService: erro ao carregar POIs — " + e.getMessage());
            todos = new ArrayList<>();
        }
    }

    private void construirIndice() {
        indiceNome = new HashMap<>();
        for (Poi p : todos) {
            // Indexar pelo nome completo normalizado
            String chave = normalizar(p.getNome());
            indiceNome.computeIfAbsent(chave, k -> new ArrayList<>()).add(p);

            // Indexar também por cada palavra do nome (>=3 chars)
            String[] palavras = chave.split("\\s+");
            for (String palavra : palavras) {
                if (palavra.length() >= 3) {
                    indiceNome.computeIfAbsent(palavra, k -> new ArrayList<>()).add(p);
                }
            }
        }
    }

    /**
     * Busca por prefixo — devolve resultados ordenados por relevância.
     * Usa o índice local, sem API externa.
     */
    public List<Poi> buscar(String query, int limite) {
        if (query == null || query.isBlank()) return Collections.emptyList();

        String q = normalizar(query.trim());
        Set<Long> vistos = new LinkedHashSet<>();
        List<Poi> resultados = new ArrayList<>();

        // 1. Correspondência exacta do nome completo
        List<Poi> exactos = indiceNome.getOrDefault(q, Collections.emptyList());
        for (Poi p : exactos) {
            if (vistos.add(p.getOsmId())) resultados.add(p);
        }

        // 2. Nome começa com o prefixo
        String qFinal = q;
        for (Map.Entry<String, List<Poi>> entry : indiceNome.entrySet()) {
            if (entry.getKey().startsWith(qFinal) && !entry.getKey().equals(qFinal)) {
                for (Poi p : entry.getValue()) {
                    if (vistos.add(p.getOsmId())) resultados.add(p);
                }
            }
            if (resultados.size() >= limite * 3) break;
        }

        // 3. Nome contém a query
        if (resultados.size() < limite) {
            for (Poi p : todos) {
                if (vistos.contains(p.getOsmId())) continue;
                if (normalizar(p.getNome()).contains(qFinal)) {
                    if (vistos.add(p.getOsmId())) resultados.add(p);
                }
                if (resultados.size() >= limite * 3) break;
            }
        }

        // Ordenar: nome mais curto primeiro (mais específico), depois alfabético
        resultados.sort(Comparator
            .comparingInt((Poi p) -> normalizar(p.getNome()).startsWith(qFinal) ? 0 : 1)
            .thenComparingInt(p -> p.getNome().length())
            .thenComparing(Poi::getNome));

        return resultados.stream().limit(limite).collect(Collectors.toList());
    }

    /**
     * Busca por categoria (amenity, shop, etc.)
     */
    public List<Poi> buscarPorCategoria(String categoria, int limite) {
        return todos.stream()
            .filter(p -> categoria.equalsIgnoreCase(p.getCategoria())
                      || categoria.equalsIgnoreCase(p.getTipo()))
            .limit(limite)
            .collect(Collectors.toList());
    }

    /**
     * Busca POIs próximos de uma coordenada (raio em metros)
     */
    public List<Poi> proximos(double lat, double lon, double raioMetros, int limite) {
        return todos.stream()
            .filter(p -> distancia(lat, lon, p.getLat(), p.getLon()) <= raioMetros)
            .sorted(Comparator.comparingDouble(p -> distancia(lat, lon, p.getLat(), p.getLon())))
            .limit(limite)
            .collect(Collectors.toList());
    }

    /**
     * Busca por ID OSM exacto
     */
    public Optional<Poi> porId(long osmId) {
        return todos.stream().filter(p -> p.getOsmId() == osmId).findFirst();
    }

    public int totalPois() { return todos.size(); }
    public List<Poi> getTodos() { return todos; }

    // Normaliza texto: minúsculas, remove acentos, remove pontuação extra
    public static String normalizar(String s) {
        if (s == null) return "";
        String r = s.toLowerCase(Locale.ROOT);
        r = r.replace("á","a").replace("à","a").replace("â","a").replace("ã","a").replace("ä","a");
        r = r.replace("é","e").replace("è","e").replace("ê","e").replace("ë","e");
        r = r.replace("í","i").replace("ì","i").replace("î","i").replace("ï","i");
        r = r.replace("ó","o").replace("ò","o").replace("ô","o").replace("õ","o").replace("ö","o");
        r = r.replace("ú","u").replace("ù","u").replace("û","u").replace("ü","u");
        r = r.replace("ç","c").replace("ñ","n");
        r = r.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        return r;
    }

    // Fórmula Haversine simplificada
    private double distancia(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}