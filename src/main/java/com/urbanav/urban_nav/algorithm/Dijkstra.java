package com.urbanav.urban_nav.algorithm;

import com.urbanav.urban_nav.graph.Grafo;
import com.urbanav.urban_nav.graph.NoGrafo;
import com.urbanav.urban_nav.heap.MinHeap;
import com.urbanav.urban_nav.model.Aresta;
import com.urbanav.urban_nav.structure.ListaLigada;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Algoritmo de Dijkstra usando MinHeap customizado (requisito 2.3 — heap optimizador).
 * Encontra o caminho mais curto entre dois nós no grafo viário de Maputo.
 * Complexidade: O((V + E) log V)
 */
@Component
public class Dijkstra {

    public ResultadoRota calcular(Grafo grafo, long idOrigem, long idDestino) {
        NoGrafo noOrigem  = grafo.buscarNo(idOrigem);
        NoGrafo noDestino = grafo.buscarNo(idDestino);

        if (noOrigem == null || noDestino == null) {
            throw new IllegalArgumentException("Origem ou destino nao encontrado no grafo.");
        }

        // Distância mínima acumulada de cada nó
        Map<Long, Double> distancia = new HashMap<>();
        // Nó anterior no caminho óptimo
        Map<Long, Long> anterior = new HashMap<>();
        // Rua usada para chegar ao nó
        Map<Long, String> ruaAnterior = new HashMap<>();

        // MinHeap customizado — substitui PriorityQueue do Java
        MinHeap heap = new MinHeap(grafo.totalNos());

        // Inicializar distâncias como infinito
        for (Long id : grafo.getNos().keySet()) {
            distancia.put(id, Double.MAX_VALUE);
        }
        distancia.put(idOrigem, 0.0);
        heap.inserir(idOrigem, 0.0);

        while (!heap.isEmpty()) {
            MinHeap.Entrada atual = heap.extrairMin();
            long idAtual = atual.idNo;
            double distAtual = atual.distancia;

            // Chegou ao destino — parar
            if (idAtual == idDestino) break;

            // Entrada obsoleta (distância maior que a actual) — ignorar
            if (distAtual > distancia.getOrDefault(idAtual, Double.MAX_VALUE)) continue;

            NoGrafo noAtual = grafo.buscarNo(idAtual);
            if (noAtual == null) continue;

            // Relaxar arestas dos vizinhos
            ListaLigada vizinhos = noAtual.getVizinhos();
            for (int i = 0; i < vizinhos.tamanho(); i++) {
                Aresta aresta = (Aresta) vizinhos.pega(i);
                long idVizinho = aresta.getDestino().getId();
                double novaDist = distAtual + aresta.getPeso();

                if (novaDist < distancia.getOrDefault(idVizinho, Double.MAX_VALUE)) {
                    distancia.put(idVizinho, novaDist);
                    anterior.put(idVizinho, idAtual);
                    ruaAnterior.put(idVizinho, aresta.getNomeRua());
                    // Inserir no MinHeap customizado
                    heap.inserir(idVizinho, novaDist);
                }
            }
        }

        return reconstruirCaminho(grafo, anterior, ruaAnterior, distancia, idOrigem, idDestino);
    }

    private ResultadoRota reconstruirCaminho(
            Grafo grafo, Map<Long, Long> anterior, Map<Long, String> ruaAnterior,
            Map<Long, Double> distancia, long idOrigem, long idDestino) {

        ListaLigada caminho = new ListaLigada();
        ListaLigada nomesDasRuas = new ListaLigada();
        double distanciaTotal = distancia.getOrDefault(idDestino, Double.MAX_VALUE);

        if (distanciaTotal == Double.MAX_VALUE) {
            return new ResultadoRota(caminho, nomesDasRuas, -1);
        }

        Long atual = idDestino;
        while (atual != null) {
            NoGrafo no = grafo.buscarNo(atual);
            if (no != null) caminho.adicionaInicio(no.getNodeOsm());
            String rua = ruaAnterior.get(atual);
            if (rua != null) nomesDasRuas.adicionaInicio(rua);
            atual = anterior.get(atual);
        }

        return new ResultadoRota(caminho, nomesDasRuas, distanciaTotal);
    }

    public static class ResultadoRota {
        private final ListaLigada caminho;
        private final ListaLigada nomesDasRuas;
        private final double distanciaTotal;

        public ResultadoRota(ListaLigada caminho, ListaLigada nomesDasRuas, double distanciaTotal) {
            this.caminho = caminho;
            this.nomesDasRuas = nomesDasRuas;
            this.distanciaTotal = distanciaTotal;
        }

        public ListaLigada getCaminho()      { return caminho; }
        public ListaLigada getNomesDasRuas() { return nomesDasRuas; }
        public double getDistanciaTotal()    { return distanciaTotal; }
        public boolean temCaminho()          { return distanciaTotal >= 0; }
    }
}