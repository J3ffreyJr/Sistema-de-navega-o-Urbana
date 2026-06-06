package com.urbanav.urban_nav.algorithm;

import com.urbanav.urban_nav.graph.Grafo;
import com.urbanav.urban_nav.graph.NoGrafo;
import com.urbanav.urban_nav.model.Aresta;
import com.urbanav.urban_nav.model.NodeOsm;
import com.urbanav.urban_nav.structure.ListaLigada;
import com.urbanav.urban_nav.structure.Pilha;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DFS {

    // Percurso em profundidade a partir de um no origem
    public ListaLigada percurso(Grafo grafo, long idOrigem) {
        NoGrafo noInicio = grafo.buscarNo(idOrigem);
        if (noInicio == null) throw new IllegalArgumentException("No de origem nao encontrado.");

        grafo.limparVisitados();
        ListaLigada visitados = new ListaLigada();
        Pilha pilha = new Pilha();

        pilha.push(noInicio);

        while (!pilha.isEmpty()) {
            NoGrafo atual = (NoGrafo) pilha.peekAndPop();

            if (!atual.getNodeOsm().isVisitado()) {
                atual.getNodeOsm().setVisitado(true);
                visitados.adicionaFim(atual.getNodeOsm());

                ListaLigada vizinhos = atual.getVizinhos();
                for (int i = 0; i < vizinhos.tamanho(); i++) {
                    Aresta aresta = (Aresta) vizinhos.pega(i);
                    NoGrafo vizinho = grafo.buscarNo(aresta.getDestino().getId());
                    if (vizinho != null && !vizinho.getNodeOsm().isVisitado()) {
                        pilha.push(vizinho);
                    }
                }
            }
        }
        return visitados;
    }

    // Encontra caminho entre origem e destino usando DFS
    public ListaLigada encontrarCaminho(Grafo grafo, long idOrigem, long idDestino) {
        NoGrafo noInicio = grafo.buscarNo(idOrigem);
        NoGrafo noFim    = grafo.buscarNo(idDestino);
        if (noInicio == null || noFim == null)
            throw new IllegalArgumentException("Origem ou destino nao encontrado.");

        grafo.limparVisitados();
        Map<Long, Long> anterior = new HashMap<>();
        Pilha pilha = new Pilha();

        pilha.push(noInicio);
        anterior.put(idOrigem, null);

        while (!pilha.isEmpty()) {
            NoGrafo atual = (NoGrafo) pilha.peekAndPop();

            if (atual.getNodeOsm().isVisitado()) continue;
            atual.getNodeOsm().setVisitado(true);

            if (atual.getNodeOsm().getId() == idDestino) {
                return reconstruirCaminho(grafo, anterior, idOrigem, idDestino);
            }

            ListaLigada vizinhos = atual.getVizinhos();
            for (int i = 0; i < vizinhos.tamanho(); i++) {
                Aresta aresta = (Aresta) vizinhos.pega(i);
                NoGrafo vizinho = grafo.buscarNo(aresta.getDestino().getId());
                if (vizinho != null && !vizinho.getNodeOsm().isVisitado()) {
                    anterior.put(vizinho.getNodeOsm().getId(), atual.getNodeOsm().getId());
                    pilha.push(vizinho);
                }
            }
        }
        return new ListaLigada(); // sem caminho
    }

    // Conta quantos nos sao alcancaveis a partir de uma origem
    public int contarAlcancaveis(Grafo grafo, long idOrigem) {
        return percurso(grafo, idOrigem).tamanho();
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
