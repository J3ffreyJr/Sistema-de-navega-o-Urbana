package com.urbanav.urban_nav.structure;

public class ListaLigada {

    private No primeiro;
    private No ultimo;
    private int tamanho;

    public ListaLigada() {
        this.primeiro = null;
        this.ultimo = null;
        this.tamanho = 0;
    }

    public void adicionaInicio(Object elemento) {
        No novoNo = new No(primeiro, elemento);
        primeiro = novoNo;
        if (tamanho == 0) ultimo = primeiro;
        tamanho++;
    }

    public void adicionaFim(Object elemento) {
        No novoNo = new No(elemento);
        if (tamanho == 0) {
            primeiro = novoNo;
            ultimo = novoNo;
        } else {
            ultimo.setProximo(novoNo);
            ultimo = novoNo;
        }
        tamanho++;
    }

    public void adicionaPosicao(int posicao, Object elemento) {
        if (posicao < 0 || posicao > tamanho)
            throw new IllegalArgumentException("Posicao invalida: " + posicao);
        if (posicao == 0) { adicionaInicio(elemento); return; }
        if (posicao == tamanho) { adicionaFim(elemento); return; }
        No anterior = pegaNo(posicao - 1);
        No novo = new No(anterior.getProximo(), elemento);
        anterior.setProximo(novo);
        tamanho++;
    }

    public Object pega(int posicao) {
        return pegaNo(posicao).getElemento();
    }

    public void removeInicio() {
        if (tamanho == 0) return;
        primeiro = primeiro.getProximo();
        tamanho--;
        if (tamanho == 0) ultimo = null;
    }

    public void removePosicao(int posicao) {
        if (posicao < 0 || posicao >= tamanho)
            throw new IllegalArgumentException("Posicao invalida: " + posicao);
        if (posicao == 0) { removeInicio(); return; }
        No anterior = pegaNo(posicao - 1);
        No paraRemover = anterior.getProximo();
        anterior.setProximo(paraRemover.getProximo());
        if (posicao == tamanho - 1) ultimo = anterior;
        tamanho--;
    }

    public void removeFim() {
        if (tamanho == 0) return;
        if (tamanho == 1) {
            primeiro = null;
            ultimo = null;
        } else {
            No penultimo = pegaNo(tamanho - 2);
            penultimo.setProximo(null);
            ultimo = penultimo;
        }
        tamanho--;
    }

    public boolean contem(Object elemento) {
        No atual = primeiro;
        while (atual != null) {
            if (atual.getElemento().equals(elemento)) return true;
            atual = atual.getProximo();
        }
        return false;
    }

    public int tamanho() {
        return tamanho;
    }

    public No pegaNo(int posicao) {
        if (posicao < 0 || posicao >= tamanho)
            throw new IllegalArgumentException("Posicao invalida: " + posicao);
        No atual = primeiro;
        for (int i = 0; i < posicao; i++) {
            atual = atual.getProximo();
        }
        return atual;
    }

    public No getPrimeiro() { return primeiro; }
    public No getUltimo()   { return ultimo; }
}
