package com.urbanav.urban_nav.graph;

import com.urbanav.urban_nav.model.Aresta;
import com.urbanav.urban_nav.model.NodeOsm;
import com.urbanav.urban_nav.structure.ListaLigada;

public class NoGrafo {

    private NodeOsm nodeOsm;
    private ListaLigada vizinhos; // lista de Aresta

    public NoGrafo(NodeOsm nodeOsm) {
        this.nodeOsm = nodeOsm;
        this.vizinhos = new ListaLigada();
    }

    public void adicionaVizinho(NodeOsm destino, double peso, String nomeRua) {
        // evita duplicatas
        for (int i = 0; i < vizinhos.tamanho(); i++) {
            Aresta a = (Aresta) vizinhos.pega(i);
            if (a.getDestino().getId() == destino.getId()) return;
        }
        vizinhos.adicionaFim(new Aresta(destino, peso, nomeRua));
    }

    public NodeOsm getNodeOsm()      { return nodeOsm; }
    public ListaLigada getVizinhos() { return vizinhos; }

    @Override
    public String toString() {
        return "NoGrafo{" + nodeOsm + ", vizinhos=" + vizinhos.tamanho() + "}";
    }
}
