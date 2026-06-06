package com.urbanav.urban_nav.trie;

import com.urbanav.urban_nav.structure.ListaLigada;
import org.springframework.stereotype.Component;

@Component
public class Trie {

    private NoTrie raiz;

    public Trie() {
        this.raiz = new NoTrie();
    }

    // Insere um nome de POI na Trie, guardando o id do no OSM associado
    public void inserir(String nome, long idOsm) {
        if (nome == null || nome.isEmpty()) return;
        String nomeMin = nome.toLowerCase();
        NoTrie atual = raiz;

        for (int i = 0; i < nomeMin.length(); i++) {
            char c = nomeMin.charAt(i);
            NoTrie filho = atual.getFilho(c);
            if (filho == null) {
                filho = new NoTrie(c);
                atual.adicionarFilho(c, filho);
            }
            atual = filho;
        }
        atual.setFimDePalavra(true);
        atual.setIdOsm(idOsm);
        atual.setNomeCompleto(nome);
    }

    // Busca exacta — retorna o id OSM se existir, -1 caso contrario
    public long buscar(String nome) {
        NoTrie no = buscarNo(nome);
        if (no != null && no.isFimDePalavra()) return no.getIdOsm();
        return -1;
    }

    // Autocompletar — retorna lista de nomes que comecem com o prefixo
    public ListaLigada autocompletar(String prefixo) {
        ListaLigada resultados = new ListaLigada();
        if (prefixo == null || prefixo.isEmpty()) return resultados;

        NoTrie noInicio = buscarNo(prefixo);
        if (noInicio == null) return resultados;

        coletarPalavras(noInicio, resultados);
        return resultados;
    }

    // Verifica se existe algum nome com este prefixo
    public boolean temPrefixo(String prefixo) {
        return buscarNo(prefixo) != null;
    }

    private NoTrie buscarNo(String texto) {
        if (texto == null) return null;
        String textoMin = texto.toLowerCase();
        NoTrie atual = raiz;
        for (int i = 0; i < textoMin.length(); i++) {
            char c = textoMin.charAt(i);
            NoTrie filho = atual.getFilho(c);
            if (filho == null) return null;
            atual = filho;
        }
        return atual;
    }

    // DFS para recolher todas as palavras a partir de um no
    private void coletarPalavras(NoTrie no, ListaLigada resultados) {
        if (no.isFimDePalavra()) {
            resultados.adicionaFim(new ResultadoTrie(no.getNomeCompleto(), no.getIdOsm()));
        }
        ListaLigada filhos = no.getFilhos();
        for (int i = 0; i < filhos.tamanho(); i++) {
            NoTrie filho = (NoTrie) filhos.pega(i);
            coletarPalavras(filho, resultados);
        }
    }

    // Classe resultado da busca
    public static class ResultadoTrie {
        private String nome;
        private long idOsm;

        public ResultadoTrie(String nome, long idOsm) {
            this.nome = nome;
            this.idOsm = idOsm;
        }

        public String getNome()  { return nome; }
        public long getIdOsm()   { return idOsm; }

        @Override
        public String toString() {
            return nome + " (id=" + idOsm + ")";
        }
    }
}
