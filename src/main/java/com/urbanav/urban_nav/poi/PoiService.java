package com.urbanav.urban_nav.poi;

import com.urbanav.urban_nav.tree.ArvoreBST;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PoiService {

    @Autowired private PoiParser poiParser;
    @Autowired private ArvoreBST arvoreBST;

    @Value("${osm.file.path:src/main/resources/data/maputo.osm}")
    private String osmFilePath;

    private List<Poi> todos = new ArrayList<>();
    private Map<String, List<Poi>> indiceNome = new HashMap<>();

    @PostConstruct
    public void inicializar() {
        try {
            todos = poiParser.parse(osmFilePath);
            construirIndice();
            construirBST();
            System.out.println("PoiService: " + todos.size() + " POIs indexados");
            System.out.println("ArvoreBST: " + arvoreBST.getTamanho() + " nós, altura=" + arvoreBST.altura());
        } catch (Exception e) {
            System.err.println("PoiService: erro — " + e.getMessage());
            todos = new ArrayList<>();
        }
    }

    private void construirIndice() {
        indiceNome = new HashMap<>();
        for (Poi p : todos) {
            String chave = normalizar(p.getNome());
            indiceNome.computeIfAbsent(chave, k -> new ArrayList<>()).add(p);
            for (String palavra : chave.split("\\s+")) {
                if (palavra.length() >= 3)
                    indiceNome.computeIfAbsent(palavra, k -> new ArrayList<>()).add(p);
            }
        }
    }

    private void construirBST() {
        for (Poi p : todos) {
            arvoreBST.inserir(p);
        }
    }

    public List<Poi> buscar(String query, int limite) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        String q = normalizar(query.trim());
        Set<Long> vistos = new LinkedHashSet<>();
        List<Poi> resultados = new ArrayList<>();

        // 1. Busca exacta na BST — O(log n)
        Poi exactoBST = arvoreBST.buscar(q);
        if (exactoBST != null && vistos.add(exactoBST.getOsmId()))
            resultados.add(exactoBST);

        // 2. Correspondência exacta no índice hash
        List<Poi> exactos = indiceNome.getOrDefault(q, Collections.emptyList());
        for (Poi p : exactos)
            if (vistos.add(p.getOsmId())) resultados.add(p);

        // 3. Prefixo na BST — percurso optimizado
        com.urbanav.urban_nav.structure.ListaLigada bstPrefixo = arvoreBST.buscarPorPrefixo(q);
        for (int i = 0; i < bstPrefixo.tamanho() && resultados.size() < limite * 2; i++) {
            Poi p = (Poi) bstPrefixo.pega(i);
            if (vistos.add(p.getOsmId())) resultados.add(p);
        }

        // 4. Prefixo no índice hash
        String qFinal = q;
        for (Map.Entry<String, List<Poi>> entry : indiceNome.entrySet()) {
            if (entry.getKey().startsWith(qFinal) && !entry.getKey().equals(qFinal)) {
                for (Poi p : entry.getValue())
                    if (vistos.add(p.getOsmId())) resultados.add(p);
            }
            if (resultados.size() >= limite * 3) break;
        }

        // 5. Contém a query
        if (resultados.size() < limite) {
            for (Poi p : todos) {
                if (vistos.contains(p.getOsmId())) continue;
                if (normalizar(p.getNome()).contains(qFinal))
                    if (vistos.add(p.getOsmId())) resultados.add(p);
                if (resultados.size() >= limite * 3) break;
            }
        }

        resultados.sort(Comparator
            .comparingInt((Poi p) -> normalizar(p.getNome()).startsWith(qFinal) ? 0 : 1)
            .thenComparingInt(p -> p.getNome().length())
            .thenComparing(Poi::getNome));

        return resultados.stream().limit(limite).collect(Collectors.toList());
    }

    public List<Poi> buscarPorCategoria(String categoria, int limite) {
        return todos.stream()
            .filter(p -> categoria.equalsIgnoreCase(p.getCategoria())
                      || categoria.equalsIgnoreCase(p.getTipo()))
            .limit(limite).collect(Collectors.toList());
    }

    public List<Poi> proximos(double lat, double lon, double raioMetros, int limite) {
        return todos.stream()
            .filter(p -> distancia(lat, lon, p.getLat(), p.getLon()) <= raioMetros)
            .sorted(Comparator.comparingDouble(p -> distancia(lat, lon, p.getLat(), p.getLon())))
            .limit(limite).collect(Collectors.toList());
    }

    public Optional<Poi> porId(long osmId) {
        return todos.stream().filter(p -> p.getOsmId() == osmId).findFirst();
    }

    // Percursos da BST expostos para demonstração académica
    public com.urbanav.urban_nav.structure.ListaLigada poisOrdenados() {
        return arvoreBST.inOrder();
    }

    public int totalPois()      { return todos.size(); }
    public List<Poi> getTodos() { return todos; }
    public ArvoreBST getArvoreBST() { return arvoreBST; }

    public static String normalizar(String s) {
        if (s == null) return "";
        String r = s.toLowerCase(java.util.Locale.ROOT);
        r = r.replace("á","a").replace("à","a").replace("â","a").replace("ã","a");
        r = r.replace("é","e").replace("è","e").replace("ê","e");
        r = r.replace("í","i").replace("ì","i").replace("î","i");
        r = r.replace("ó","o").replace("ò","o").replace("ô","o").replace("õ","o");
        r = r.replace("ú","u").replace("ù","u").replace("û","u");
        r = r.replace("ç","c").replace("ñ","n");
        r = r.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        return r;
    }

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