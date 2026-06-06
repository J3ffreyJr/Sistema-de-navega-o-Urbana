package com.urbanav.urban_nav.graph;

import com.urbanav.urban_nav.parser.OsmParser;
import com.urbanav.urban_nav.trie.Trie;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

    @Autowired
    private OsmParser osmParser;

    @Autowired
    private Trie trie;

    private Grafo grafo;

    @PostConstruct
    public void inicializar() throws Exception {
        System.out.println("=== Urban Nav: Iniciando carregamento do OSM ===");
        long inicio = System.currentTimeMillis();

        grafo = osmParser.parse();

        grafo.getNos().values().forEach(noGrafo -> {
            String nome = noGrafo.getNodeOsm().getNome();
            if (nome != null && !nome.isEmpty()) {
                trie.inserir(nome, noGrafo.getNodeOsm().getId());
            }
        });

        long fim = System.currentTimeMillis();
        System.out.println("=== Grafo pronto em " + (fim - inicio) + "ms ===");
        System.out.println("    Nos: " + grafo.totalNos());
        System.out.println("    Arestas: " + grafo.getTotalArestas());
    }

    public Grafo getGrafo() {
        return grafo;
    }
}