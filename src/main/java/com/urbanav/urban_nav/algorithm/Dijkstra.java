package com.urbanav.urban_nav.algorithm;

import com.urbanav.urban_nav.graph.Grafo;
import com.urbanav.urban_nav.graph.NoGrafo;
import com.urbanav.urban_nav.model.Aresta;
import com.urbanav.urban_nav.model.NodeOsm;
import com.urbanav.urban_nav.structure.ListaLigada;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Component
public class Dijkstra {

    public ResultadoRota calcular(Grafo grafo, long idOrigem, long idDestino) {
        NoGrafo noOrigem  = grafo.buscarNo(idOrigem);
        NoGrafo noDestino = grafo.buscarNo(idDestino);

        if (noOrigem == null || noDestino == null) {
            throw new IllegalArgumentException("Origem ou destino nao encontrado no grafo.");
        }

        // distancia minima acumulada de cada no
        Map<Long, Double> distancia = new HashMap<>();
        // no anterior no caminho otimo
        Map<Long, Long> anterior = new HashMap<>();
        // rua usada para chegar ao no
        Map<Long, String> ruaAnterior = new HashMap<>();

        // Fila de prioridade: [distancia, idNo]
        PriorityQueue<long[]> filaPrioridade = new PriorityQueue<>(
                (a, b) -> Double.compare(Double.longBitsToDouble(a[1]), Double.longBitsToDouble(b[1]))
        );

        // Inicializar todas as distancias como infinito
        for (Long id : grafo.getNos().keySet()) {
            distancia.put(id, Double.MAX_VALUE);
        }
        distancia.put(idOrigem, 0.0);
        filaPrioridade.offer(new long[]{idOrigem, Double.doubleToLongBits(0.0)});

        while (!filaPrioridade.isEmpty()) {
            long[] atual = filaPrioridade.poll();
            long idAtual = atual[0];
            double distAtual = Double.longBitsToDouble(atual[1]);

            // Se chegamos ao destino, podemos parar
            if (idAtual == idDestino) break;

            // Ignorar se ja encontramos caminho melhor
            if (distAtual > distancia.get(idAtual)) continue;

            NoGrafo noAtual = grafo.buscarNo(idAtual);
            if (noAtual == null) continue;

            ListaLigada vizinhos = noAtual.getVizinhos();
            for (int i = 0; i < vizinhos.tamanho(); i++) {
                Aresta aresta = (Aresta) vizinhos.pega(i);
                long idVizinho = aresta.getDestino().getId();
                double novaDist = distAtual + aresta.getPeso();

                if (novaDist < distancia.getOrDefault(idVizinho, Double.MAX_VALUE)) {
                    distancia.put(idVizinho, novaDist);
                    anterior.put(idVizinho, idAtual);
                    ruaAnterior.put(idVizinho, aresta.getNomeRua());
                    filaPrioridade.offer(new long[]{idVizinho, Double.doubleToLongBits(novaDist)});
                }
            }
        }

        // Reconstruir caminho
        return reconstruirCaminho(grafo, anterior, ruaAnterior, distancia, idOrigem, idDestino);
    }

    private ResultadoRota reconstruirCaminho(
            Grafo grafo,
            Map<Long, Long> anterior,
            Map<Long, String> ruaAnterior,
            Map<Long, Double> distancia,
            long idOrigem, long idDestino) {

        ListaLigada caminho = new ListaLigada();
        ListaLigada nomesDasRuas = new ListaLigada();
        double distanciaTotal = distancia.getOrDefault(idDestino, Double.MAX_VALUE);

        if (distanciaTotal == Double.MAX_VALUE) {
            // Sem caminho
            return new ResultadoRota(caminho, nomesDasRuas, -1);
        }

        // Percorrer para tras a partir do destino
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

    // Classe resultado
    public static class ResultadoRota {
        private ListaLigada caminho;       // lista de NodeOsm
        private ListaLigada nomesDasRuas;  // lista de String
        private double distanciaTotal;     // em metros

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
