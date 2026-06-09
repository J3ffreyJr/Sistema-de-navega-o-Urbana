package com.urbanav.urban_nav.api;

import com.urbanav.urban_nav.heap.MinHeap;
import com.urbanav.urban_nav.poi.Poi;
import com.urbanav.urban_nav.poi.PoiService;
import com.urbanav.urban_nav.structure.ListaLigada;
import com.urbanav.urban_nav.tree.ArvoreBST;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class TreeController {

    @Autowired private PoiService poiService;

    @GetMapping("/bst/stats")
    public ResponseEntity<Map<String, Object>> bstStats() {
        ArvoreBST bst = poiService.getArvoreBST();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("estrutura", "Árvore Binária de Pesquisa (BST)");
        resp.put("totalNos", bst.getTamanho());
        resp.put("altura", bst.altura());
        resp.put("vazia", bst.isEmpty());
        resp.put("complexidadeBusca", "O(log n) médio");
        resp.put("complexidadeInsercao", "O(log n) médio");
        resp.put("complexidadePercurso", "O(n)");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/bst/inorder")
    public ResponseEntity<Map<String, Object>> inOrder(@RequestParam(defaultValue = "20") int limite) {
        ListaLigada lista = poiService.poisOrdenados();
        return ResponseEntity.ok(formatarPercurso("In-Order (alfabético)", lista, limite));
    }

    @GetMapping("/bst/preorder")
    public ResponseEntity<Map<String, Object>> preOrder(@RequestParam(defaultValue = "20") int limite) {
        ListaLigada lista = poiService.getArvoreBST().preOrder();
        return ResponseEntity.ok(formatarPercurso("Pre-Order", lista, limite));
    }

    @GetMapping("/bst/posorder")
    public ResponseEntity<Map<String, Object>> posOrder(@RequestParam(defaultValue = "20") int limite) {
        ListaLigada lista = poiService.getArvoreBST().posOrder();
        return ResponseEntity.ok(formatarPercurso("Pos-Order", lista, limite));
    }

    @GetMapping("/bst/busca")
    public ResponseEntity<Map<String, Object>> bstBusca(@RequestParam String nome) {
        long t0 = System.nanoTime();
        Poi poi = poiService.getArvoreBST().buscar(nome);
        long micros = (System.nanoTime() - t0) / 1000;
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("query", nome);
        resp.put("tempoMicros", micros);
        resp.put("encontrado", poi != null);
        if (poi != null) {
            resp.put("nome", poi.getNome());
            resp.put("lat", poi.getLat());
            resp.put("lon", poi.getLon());
            resp.put("tipo", poi.getTipo());
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/heap/demo")
    public ResponseEntity<Map<String, Object>> heapDemo() {
        MinHeap heap = new MinHeap(16);
        Map<String, Object> resp = new LinkedHashMap<>();
        double[] distancias = {5.2, 1.8, 9.0, 3.4, 7.1, 2.6, 8.3, 0.9};
        long[]   ids        = {101, 202, 303, 404, 505, 606, 707, 808};

        List<Map<String, Object>> insercoes = new ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            heap.inserir(ids[i], distancias[i]);
            Map<String, Object> op = new LinkedHashMap<>();
            op.put("idNo", ids[i]);
            op.put("distancia", distancias[i]);
            op.put("tamanhoHeap", heap.tamanho());
            op.put("minActual", heap.peek().distancia);
            insercoes.add(op);
        }
        resp.put("insercoes", insercoes);
        resp.put("heapValido", heap.validar());

        List<Map<String, Object>> extraccoes = new ArrayList<>();
        while (!heap.isEmpty()) {
            MinHeap.Entrada e = heap.extrairMin();
            Map<String, Object> op = new LinkedHashMap<>();
            op.put("idNo", e.idNo);
            op.put("distancia", e.distancia);
            extraccoes.add(op);
        }
        resp.put("extraccoes", extraccoes);
        resp.put("nota", "Extracções em ordem crescente — propriedade min-heap garantida");
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> formatarPercurso(String tipo, ListaLigada lista, int limite) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("percurso", tipo);
        resp.put("totalNos", lista.tamanho());
        List<Map<String, Object>> pois = new ArrayList<>();
        int max = Math.min(limite, lista.tamanho());
        for (int i = 0; i < max; i++) {
            Poi p = (Poi) lista.pega(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nome", p.getNome());
            item.put("tipo", p.getTipo());
            item.put("lat", p.getLat());
            item.put("lon", p.getLon());
            pois.add(item);
        }
        resp.put("pois", pois);
        if (lista.tamanho() > limite)
            resp.put("nota", "Mostrando " + limite + " de " + lista.tamanho());
        return resp;
    }
}