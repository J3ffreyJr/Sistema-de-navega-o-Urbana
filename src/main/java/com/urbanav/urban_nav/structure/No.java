package com.urbanav.urban_nav.structure;

public class No {

    private No proximo;
    private Object elemento;

    public No(No proximo, Object elemento) {
        this.proximo = proximo;
        this.elemento = elemento;
    }

    public No(Object elemento) {
        this.proximo = null;
        this.elemento = elemento;
    }

    public No getProximo() { return proximo; }
    public void setProximo(No proximo) { this.proximo = proximo; }
    public Object getElemento() { return elemento; }
    public void setElemento(Object elemento) { this.elemento = elemento; }
}
