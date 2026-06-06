package com.urbanav.urban_nav.api;

import com.urbanav.urban_nav.algorithm.BFS;
import com.urbanav.urban_nav.algorithm.DFS;
import com.urbanav.urban_nav.algorithm.Dijkstra;
import com.urbanav.urban_nav.graph.Grafo;
import com.urbanav.urban_nav.graph.GraphService;
import com.urbanav.urban_nav.model.NodeOsm;
import com.urbanav.urban_nav.api.poi.Poi;
import com.urbanav.urban_nav.api.poi.PoiParser;
import com.urbanav.urban_nav.api.poi.PoiService;
import com.urbanav.urban_nav.structure.ListaLigada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RouteController {

    @Autowired private GraphService graphService;
    @Autowired private Dijkstra dijkstra;
    @Autowired private BFS bfs;
    @Autowired private DFS dfs;
    @Autowired private PoiService poiService;
    @Autowired private PoiParser poiParser;

    // GET /api/status
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Grafo grafo = graphService.getGrafo();
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "online");
        resp.put("nos", grafo.totalNos());
        resp.put("arestas", grafo.getTotalArestas());
        resp.put("pois", poiService.totalPois());
        return ResponseEntity.ok(resp);
    }

    // GET /api/rota/dijkstra
    @GetMapping("/rota/dijkstra")
    public ResponseEntity<Map<String, Object>> rotaDijkstra(
            @RequestParam double origemLat, @RequestParam double origemLon,
            @RequestParam double destinoLat, @RequestParam double destinoLon) {
        Grafo grafo = graphService.getGrafo();
        NodeOsm origem  = grafo.nodoMaisProximo(origemLat, origemLon);
        NodeOsm destino = grafo.nodoMaisProximo(destinoLat, destinoLon);
        Dijkstra.ResultadoRota resultado = dijkstra.calcular(grafo, origem.getId(), destino.getId());
        return ResponseEntity.ok(formatarRota(resultado.getCaminho(), resultado.getDistanciaTotal(), "dijkstra"));
    }

    // GET /api/rota/bfs
    @GetMapping("/rota/bfs")
    public ResponseEntity<Map<String, Object>> rotaBFS(
            @RequestParam double origemLat, @RequestParam double origemLon,
            @RequestParam double destinoLat, @RequestParam double destinoLon) {
        Grafo grafo = graphService.getGrafo();
        NodeOsm origem  = grafo.nodoMaisProximo(origemLat, origemLon);
        NodeOsm destino = grafo.nodoMaisProximo(destinoLat, destinoLon);
        ListaLigada caminho = bfs.encontrarCaminho(grafo, origem.getId(), destino.getId());
        return ResponseEntity.ok(formatarRota(caminho, -1, "bfs"));
    }

    // GET /api/rota/dfs
    @GetMapping("/rota/dfs")
    public ResponseEntity<Map<String, Object>> rotaDFS(
            @RequestParam double origemLat, @RequestParam double origemLon,
            @RequestParam double destinoLat, @RequestParam double destinoLon) {
        Grafo grafo = graphService.getGrafo();
        NodeOsm origem  = grafo.nodoMaisProximo(origemLat, origemLon);
        NodeOsm destino = grafo.nodoMaisProximo(destinoLat, destinoLon);
        ListaLigada caminho = dfs.encontrarCaminho(grafo, origem.getId(), destino.getId());
        return ResponseEntity.ok(formatarRota(caminho, -1, "dfs"));
    }

    // GET /api/poi/busca?prefixo=... (compatibilidade com frontend antigo)
    @GetMapping("/poi/busca")
    public ResponseEntity<List<Map<String, Object>>> buscarPOILegado(@RequestParam String prefixo) {
        List<Poi> resultados = poiService.buscar(prefixo, 10);
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Poi p : resultados) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nome", p.getNome());
            item.put("idOsm", p.getOsmId());
            item.put("lat", p.getLat());
            item.put("lon", p.getLon());
            item.put("tipo", poiParser.getLabelTipo(p.getTipo()));
            if (p.getMorada() != null) item.put("morada", p.getMorada());
            lista.add(item);
        }
        return ResponseEntity.ok(lista);
    }

    // GET /api/no/proximo
    @GetMapping("/no/proximo")
    public ResponseEntity<Map<String, Object>> noMaisProximo(
            @RequestParam double lat, @RequestParam double lon) {
        NodeOsm no = graphService.getGrafo().nodoMaisProximo(lat, lon);
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", no.getId()); resp.put("lat", no.getLat());
        resp.put("lon", no.getLon()); resp.put("nome", no.getNome());
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> formatarRota(ListaLigada caminho, double distancia, String algoritmo) {
        List<Map<String, Object>> pontos = new ArrayList<>();
        for (int i = 0; i < caminho.tamanho(); i++) {
            NodeOsm n = (NodeOsm) caminho.pega(i);
            Map<String, Object> ponto = new HashMap<>();
            ponto.put("lat", n.getLat()); ponto.put("lon", n.getLon()); ponto.put("id", n.getId());
            pontos.add(ponto);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("algoritmo", algoritmo); resp.put("caminho", pontos);
        resp.put("totalNos", caminho.tamanho());
        if (distancia >= 0) resp.put("distanciaMetros", Math.round(distancia));
        return resp;
    }
}