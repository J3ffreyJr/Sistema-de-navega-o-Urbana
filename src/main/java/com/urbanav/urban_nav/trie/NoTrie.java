package com.urbanav.urban_nav.trie;

import com.urbanav.urban_nav.structure.ListaLigada;

public class NoTrie {

    private char caractere;
    private boolean fimDePalavra;
    private long idOsm;
    private String nomeCompleto;
    private ListaLigada filhos; // lista de NoTrie

    public NoTrie() {
        this.filhos = new ListaLigada();
    }

    public NoTrie(char caractere) {
        this.caractere = caractere;
        this.filhos = new ListaLigada();
    }

    public void adicionarFilho(char c, NoTrie filho) {
        filhos.adicionaFim(filho);
    }

    public NoTrie getFilho(char c) {
        for (int i = 0; i < filhos.tamanho(); i++) {
            NoTrie filho = (NoTrie) filhos.pega(i);
            if (filho.getCaractere() == c) return filho;
        }
        return null;
    }

    public ListaLigada getFilhos()             { return filhos; }
    public char getCaractere()                 { return caractere; }
    public boolean isFimDePalavra()            { return fimDePalavra; }
    public void setFimDePalavra(boolean fim)   { this.fimDePalavra = fim; }
    public long getIdOsm()                     { return idOsm; }
    public void setIdOsm(long idOsm)           { this.idOsm = idOsm; }
    public String getNomeCompleto()            { return nomeCompleto; }
    public void setNomeCompleto(String nome)   { this.nomeCompleto = nome; }
}
