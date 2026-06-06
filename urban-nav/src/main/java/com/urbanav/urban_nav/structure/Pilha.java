package com.urbanav.urban_nav.structure;

public class Pilha {

    private No topo;
    private int tamanho;

    public Pilha() {
        this.topo = null;
        this.tamanho = 0;
    }

    public void push(Object elemento) {
        No novoNo = new No(elemento);
        if (!isEmpty()) novoNo.setProximo(topo);
        topo = novoNo;
        tamanho++;
    }

    public void pop() {
        if (isEmpty()) throw new IllegalStateException("Pilha vazia");
        topo = topo.getProximo();
        tamanho--;
    }

    public Object peek() {
        if (isEmpty()) throw new IllegalStateException("Pilha vazia");
        return topo.getElemento();
    }

    public Object peekAndPop() {
        Object elemento = peek();
        pop();
        return elemento;
    }

    public boolean isEmpty() { return topo == null; }
    public int size()        { return tamanho; }
}
