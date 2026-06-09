package com.urbanav.urban_nav.tree;

import com.urbanav.urban_nav.poi.Poi;

public class NoBST {

    private String chave;
    private Poi poi;
    private NoBST esq;
    private NoBST dir;

    public NoBST(String chave, Poi poi) {
        this.chave = chave;
        this.poi   = poi;
        this.esq   = null;
        this.dir   = null;
    }

    public String getChave()       { return chave; }
    public Poi    getPoi()         { return poi; }
    public NoBST  getEsq()         { return esq; }
    public NoBST  getDir()         { return dir; }
    public void   setEsq(NoBST e)  { this.esq = e; }
    public void   setDir(NoBST d)  { this.dir = d; }

    @Override
    public String toString() {
        return "NoBST{chave='" + chave + "', poi=" + poi.getNome() + "}";
    }
}