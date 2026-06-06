package com.urbanav.urban_nav.structure;

public class Fila {

    private No primeiro;
    private No ultimo;
    private int tamanho;

    public Fila() {
        this.primeiro = null;
        this.ultimo = null;
        this.tamanho = 0;
    }

    public void enqueue(Object elemento) {
        No novoNo = new No(elemento);
        if (isEmpty()) {
            primeiro = novoNo;
            ultimo = novoNo;
        } else {
            ultimo.setProximo(novoNo);
            ultimo = novoNo;
        }
        tamanho++;
    }

    public void dequeue() {
        if (isEmpty()) throw new IllegalStateException("Fila vazia");
        primeiro = primeiro.getProximo();
        tamanho--;
        if (isEmpty()) ultimo = null;
    }

    public Object peek() {
        if (isEmpty()) throw new IllegalStateException("Fila vazia");
        return primeiro.getElemento();
    }

    public Object peekAndDequeue() {
        Object elemento = peek();
        dequeue();
        return elemento;
    }

    public boolean isEmpty() { return primeiro == null; }
    public int size()        { return tamanho; }
}
