package com.urbanav.urban_nav.algorithm;

import com.urbanav.urban_nav.graph.Grafo;
import com.urbanav.urban_nav.graph.NoGrafo;
import com.urbanav.urban_nav.model.Aresta;
import com.urbanav.urban_nav.model.NodeOsm;
import com.urbanav.urban_nav.structure.Fila;
import com.urbanav.urban_nav.structure.ListaLigada;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BFS {

    // Percurso em largura a partir de um no origem
    public ListaLigada percurso(Grafo grafo, long idOrigem) {
        NoGrafo noInicio = grafo.buscarNo(idOrigem);
        if (noInicio == null) throw new IllegalArgumentException("No de origem nao encontrado.");

        grafo.limparVisitados();
        ListaLigada visitados = new ListaLigada();
        Fila fila = new Fila();

        noInicio.getNodeOsm().setVisitado(true);
        fila.enqueue(noInicio);

        while (!fila.isEmpty()) {
            NoGrafo atual = (NoGrafo) fila.peekAndDequeue();
            visitados.adicionaFim(atual.getNodeOsm());

            ListaLigada vizinhos = atual.getVizinhos();
            for (int i = 0; i < vizinhos.tamanho(); i++) {
                Aresta aresta = (Aresta) vizinhos.pega(i);
                NoGrafo vizinho = grafo.buscarNo(aresta.getDestino().getId());
                if (vizinho != null && !vizinho.getNodeOsm().isVisitado()) {
                    vizinho.getNodeOsm().setVisitado(true);
                    fila.enqueue(vizinho);
                }
            }
        }
        return visitados;
    }

    // Encontra caminho entre origem e destino usando BFS (nao ponderado)
    public ListaLigada encontrarCaminho(Grafo grafo, long idOrigem, long idDestino) {
        NoGrafo noInicio  = grafo.buscarNo(idOrigem);
        NoGrafo noFim     = grafo.buscarNo(idDestino);
        if (noInicio == null || noFim == null)
            throw new IllegalArgumentException("Origem ou destino nao encontrado.");

        grafo.limparVisitados();
        Map<Long, Long> anterior = new HashMap<>();
        Fila fila = new Fila();

        noInicio.getNodeOsm().setVisitado(true);
        fila.enqueue(noInicio);
        anterior.put(idOrigem, null);

        while (!fila.isEmpty()) {
            NoGrafo atual = (NoGrafo) fila.peekAndDequeue();

            if (atual.getNodeOsm().getId() == idDestino) {
                return reconstruirCaminho(grafo, anterior, idOrigem, idDestino);
            }

            ListaLigada vizinhos = atual.getVizinhos();
            for (int i = 0; i < vizinhos.tamanho(); i++) {
                Aresta aresta = (Aresta) vizinhos.pega(i);
                NoGrafo vizinho = grafo.buscarNo(aresta.getDestino().getId());
                if (vizinho != null && !vizinho.getNodeOsm().isVisitado()) {
                    vizinho.getNodeOsm().setVisitado(true);
                    anterior.put(vizinho.getNodeOsm().getId(), atual.getNodeOsm().getId());
                    fila.enqueue(vizinho);
                }
            }
        }
        return new ListaLigada(); // sem caminho
    }

    // Verifica se existe caminho entre dois nos
    public boolean existeCaminho(Grafo grafo, long idOrigem, long idDestino) {
        ListaLigada caminho = encontrarCaminho(grafo, idOrigem, idDestino);
        return caminho.tamanho() > 0;
    }

    private ListaLigada reconstruirCaminho(Grafo grafo, Map<Long, Long> anterior,
                                            long idOrigem, long idDestino) {
        ListaLigada caminho = new ListaLigada();
        Long atual = idDestino;
        while (atual != null) {
            NoGrafo no = grafo.buscarNo(atual);
            if (no != null) caminho.adicionaInicio(no.getNodeOsm());
            atual = anterior.get(atual);
        }
        return caminho;
    }
}
