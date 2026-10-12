package com.urbanav.urban_nav.api;

import com.urbanav.urban_nav.algorithm.BFS;
import com.urbanav.urban_nav.algorithm.DFS;
import com.urbanav.urban_nav.algorithm.Dijkstra;
import com.urbanav.urban_nav.graph.Grafo;
import com.urbanav.urban_nav.graph.GraphService;
import com.urbanav.urban_nav.model.NodeOsm;
import com.urbanav.urban_nav.poi.Poi;
import com.urbanav.urban_nav.poi.PoiParser;
import com.urbanav.urban_nav.poi.PoiService;
import com.urbanav.urban_nav.structure.ListaLigada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RouteController {

    private static final double MAX_SNAP_DISTANCE_METERS = 300.0;

    @Autowired private GraphService graphService;
    @Autowired private Dijkstra dijkstra;
    @Autowired private BFS bfs;
    @Autowired private DFS dfs;
    @Autowired private PoiService poiService;
    @Autowired private PoiParser poiParser;

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

    @GetMapping("/rota/dijkstra")
    public ResponseEntity<Map<String, Object>> rotaDijkstra(
            @RequestParam double origemLat, @RequestParam double origemLon,
            @RequestParam double destinoLat, @RequestParam double destinoLon) {
        Grafo grafo = graphService.getGrafo();
        NodeOsm origem  = noViarioValido(grafo, origemLat, origemLon, "origem");
        NodeOsm destino = noViarioValido(grafo, destinoLat, destinoLon, "destino");
        Dijkstra.ResultadoRota resultado = dijkstra.calcular(grafo, origem.getId(), destino.getId());
        return ResponseEntity.ok(formatarRota(resultado.getCaminho(), resultado.getDistanciaTotal(), "dijkstra"));
    }

    @GetMapping("/rota/bfs")
    public ResponseEntity<Map<String, Object>> rotaBFS(
            @RequestParam double origemLat, @RequestParam double origemLon,
            @RequestParam double destinoLat, @RequestParam double destinoLon) {
        Grafo grafo = graphService.getGrafo();
        NodeOsm origem  = noViarioValido(grafo, origemLat, origemLon, "origem");
        NodeOsm destino = noViarioValido(grafo, destinoLat, destinoLon, "destino");
        ListaLigada caminho = bfs.encontrarCaminho(grafo, origem.getId(), destino.getId());
        return ResponseEntity.ok(formatarRota(caminho, -1, "bfs"));
    }

    @GetMapping("/rota/dfs")
    public ResponseEntity<Map<String, Object>> rotaDFS(
            @RequestParam double origemLat, @RequestParam double origemLon,
            @RequestParam double destinoLat, @RequestParam double destinoLon) {
        Grafo grafo = graphService.getGrafo();
        NodeOsm origem  = noViarioValido(grafo, origemLat, origemLon, "origem");
        NodeOsm destino = noViarioValido(grafo, destinoLat, destinoLon, "destino");
        ListaLigada caminho = dfs.encontrarCaminho(grafo, origem.getId(), destino.getId());
        return ResponseEntity.ok(formatarRota(caminho, -1, "dfs"));
    }

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

    @GetMapping("/no/proximo")
    public ResponseEntity<Map<String, Object>> noMaisProximo(
            @RequestParam double lat, @RequestParam double lon) {
        NodeOsm no = graphService.getGrafo().nodoMaisProximo(lat, lon);
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", no.getId());
        resp.put("lat", no.getLat());
        resp.put("lon", no.getLon());
        resp.put("nome", no.getNome());
        return ResponseEntity.ok(resp);
    }

    private NodeOsm noViarioValido(Grafo grafo, double lat, double lon, String nome) {
        if (!Double.isFinite(lat) || !Double.isFinite(lon)) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Coordenadas inválidas para " + nome
            );
        }
        NodeOsm no = grafo.nodoMaisProximo(lat, lon);
        double distancia = grafo.distanciaAoNoMaisProximo(lat, lon);
        if (no == null || distancia > MAX_SNAP_DISTANCE_METERS) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                "A " + nome + " está a " + Math.round(distancia)
                    + " m da rede viária; máximo permitido: "
                    + Math.round(MAX_SNAP_DISTANCE_METERS) + " m"
            );
        }
        return no;
    }

    private Map<String, Object> formatarRota(ListaLigada caminho, double distancia, String algoritmo) {
        List<Map<String, Object>> pontos = new ArrayList<>();
        for (int i = 0; i < caminho.tamanho(); i++) {
            NodeOsm n = (NodeOsm) caminho.pega(i);
            Map<String, Object> ponto = new HashMap<>();
            ponto.put("lat", n.getLat());
            ponto.put("lon", n.getLon());
            ponto.put("id", n.getId());
            pontos.add(ponto);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("algoritmo", algoritmo);
        resp.put("caminho", pontos);
        resp.put("totalNos", caminho.tamanho());
        if (distancia >= 0) resp.put("distanciaMetros", Math.round(distancia));
        return resp;
    }
}