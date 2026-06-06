package com.urbanav.urban_nav.graph;

import com.urbanav.urban_nav.model.Aresta;
import com.urbanav.urban_nav.model.NodeOsm;
import com.urbanav.urban_nav.structure.ListaLigada;

import java.util.HashMap;
import java.util.Map;

public class Grafo {

    // HashMap para acesso O(1) por ID do no OSM
    private Map<Long, NoGrafo> nos;
    private int totalArestas;

    public Grafo() {
        this.nos = new HashMap<>();
        this.totalArestas = 0;
    }

    // Adiciona um no ao grafo
    public void adicionarNo(NodeOsm node) {
        if (!nos.containsKey(node.getId())) {
            node.setIndice(nos.size());
            nos.put(node.getId(), new NoGrafo(node));
        }
    }

    // Adiciona aresta bidirecional entre dois nos
    public void adicionarAresta(long idOrigem, long idDestino, String nomeRua) {
        NoGrafo origem  = nos.get(idOrigem);
        NoGrafo destino = nos.get(idDestino);
        if (origem == null || destino == null) return;

        double peso = origem.getNodeOsm().distanciaAte(destino.getNodeOsm());
        origem.adicionaVizinho(destino.getNodeOsm(), peso, nomeRua);
        destino.adicionaVizinho(origem.getNodeOsm(), peso, nomeRua);
        totalArestas++;
    }

    // Busca um NoGrafo pelo ID OSM
    public NoGrafo buscarNo(long id) {
        return nos.get(id);
    }

    // Retorna o NodeOsm mais proximo de uma coordenada (para snap ao grafo)
    public NodeOsm nodoMaisProximo(double lat, double lon) {
        NodeOsm maisProximo = null;
        double menorDist = Double.MAX_VALUE;

        for (NoGrafo noGrafo : nos.values()) {
            NodeOsm n = noGrafo.getNodeOsm();
            double dLat = n.getLat() - lat;
            double dLon = n.getLon() - lon;
            double dist = dLat * dLat + dLon * dLon; // euclidiana aproximada
            if (dist < menorDist) {
                menorDist = dist;
                maisProximo = n;
            }
        }
        return maisProximo;
    }

    // Limpa flags de visitado em todos os nos (para BFS/DFS)
    public void limparVisitados() {
        for (NoGrafo noGrafo : nos.values()) {
            noGrafo.getNodeOsm().setVisitado(false);
        }
    }

    // Retorna todos os NoGrafo como ListaLigada
    public ListaLigada getTodosNos() {
        ListaLigada lista = new ListaLigada();
        for (NoGrafo noGrafo : nos.values()) {
            lista.adicionaFim(noGrafo);
        }
        return lista;
    }

    public Map<Long, NoGrafo> getNos() { return nos; }
    public int totalNos()              { return nos.size(); }
    public int getTotalArestas()       { return totalArestas; }

    public void imprimir() {
        int max = Math.min(nos.size(), 10); // imprime apenas os primeiros 10
        int count = 0;
        for (NoGrafo noGrafo : nos.values()) {
            NodeOsm n = noGrafo.getNodeOsm();
            System.out.print("No " + n.getId() + ": ");
            ListaLigada viz = noGrafo.getVizinhos();
            for (int j = 0; j < viz.tamanho(); j++) {
                Aresta a = (Aresta) viz.pega(j);
                System.out.print(a.getDestino().getId()
                        + "(" + String.format("%.0f", a.getPeso()) + "m)");
                if (j < viz.tamanho() - 1) System.out.print(", ");
            }
            System.out.println();
            if (++count >= max) break;
        }
        System.out.println("... Total: " + nos.size() + " nos, " + totalArestas + " arestas");
    }
}
