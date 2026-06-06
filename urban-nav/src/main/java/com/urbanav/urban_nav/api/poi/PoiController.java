package com.urbanav.urban_nav.api.poi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/pois")
@CrossOrigin(origins = "*")
public class PoiController {

    @Autowired
    private PoiService poiService;

    @Autowired
    private PoiParser poiParser;

    /**
     * GET /api/pois/busca?q=baia+mall&limite=10
     * Busca por nome — fonte: ficheiro OSM local, sem API externa
     */
    @GetMapping("/busca")
    public ResponseEntity<List<Map<String, Object>>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limite) {

        List<Poi> resultados = poiService.buscar(q, limite);
        return ResponseEntity.ok(serializar(resultados));
    }

    /**
     * GET /api/pois/proximos?lat=-25.96&lon=32.58&raio=500&limite=10
     * POIs num raio (metros) de uma coordenada
     */
    @GetMapping("/proximos")
    public ResponseEntity<List<Map<String, Object>>> proximos(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "500") double raio,
            @RequestParam(defaultValue = "10") int limite) {

        List<Poi> resultados = poiService.proximos(lat, lon, raio, limite);
        return ResponseEntity.ok(serializar(resultados));
    }

    /**
     * GET /api/pois/categoria?tipo=supermarket&limite=20
     */
    @GetMapping("/categoria")
    public ResponseEntity<List<Map<String, Object>>> categoria(
            @RequestParam String tipo,
            @RequestParam(defaultValue = "20") int limite) {

        List<Poi> resultados = poiService.buscarPorCategoria(tipo, limite);
        return ResponseEntity.ok(serializar(resultados));
    }

    /**
     * GET /api/pois/stats
     * Estatísticas do índice de POIs
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("total", poiService.totalPois());
        resp.put("fonte", "maputo.osm (local)");
        resp.put("apiExterna", false);

        // Contar por categoria
        Map<String, Long> porCat = new HashMap<>();
        for (Poi p : poiService.getTodos()) {
            String label = poiParser.getLabelTipo(p.getTipo());
            porCat.merge(label, 1L, Long::sum);
        }
        // Top 15 categorias
        porCat.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(15)
            .forEach(e -> resp.put("cat_" + e.getKey(), e.getValue()));

        return ResponseEntity.ok(resp);
    }

    // Serializa lista de Poi para JSON
    private List<Map<String, Object>> serializar(List<Poi> pois) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Poi p : pois) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("osmId", p.getOsmId());
            m.put("nome", p.getNome());
            m.put("lat", p.getLat());
            m.put("lon", p.getLon());
            m.put("categoria", p.getCategoria());
            m.put("tipo", p.getTipo());
            m.put("tipoLabel", poiParser.getLabelTipo(p.getTipo()));
            if (p.getMorada() != null)   m.put("morada",   p.getMorada());
            if (p.getTelefone() != null) m.put("telefone", p.getTelefone());
            if (p.getWebsite() != null)  m.put("website",  p.getWebsite());
            if (p.getHorario() != null)  m.put("horario",  p.getHorario());
            lista.add(m);
        }
        return lista;
    }
}