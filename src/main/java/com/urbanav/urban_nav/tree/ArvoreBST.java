package com.urbanav.urban_nav.tree;

import com.urbanav.urban_nav.poi.Poi;
import com.urbanav.urban_nav.structure.ListaLigada;
import org.springframework.stereotype.Component;

@Component
public class ArvoreBST {

    private NoBST raiz;
    private int tamanho;

    public ArvoreBST() {
        this.raiz    = null;
        this.tamanho = 0;
    }

    // ── INSERÇÃO ──────────────────────────────────────────────────────────────
    public void inserir(Poi poi) {
        String chave = normalizar(poi.getNome());
        if (chave.isEmpty()) return;
        raiz = inserirRec(raiz, chave, poi);
    }

    private NoBST inserirRec(NoBST no, String chave, Poi poi) {
        if (no == null) { tamanho++; return new NoBST(chave, poi); }
        int cmp = chave.compareTo(no.getChave());
        if      (cmp < 0) no.setEsq(inserirRec(no.getEsq(), chave, poi));
        else if (cmp > 0) no.setDir(inserirRec(no.getDir(), chave, poi));
        return no;
    }

    // ── BUSCA EXACTA ──────────────────────────────────────────────────────────
    public Poi buscar(String nome) {
        NoBST no = buscarNo(raiz, normalizar(nome));
        return no != null ? no.getPoi() : null;
    }

    private NoBST buscarNo(NoBST no, String chave) {
        if (no == null) return null;
        int cmp = chave.compareTo(no.getChave());
        if      (cmp < 0) return buscarNo(no.getEsq(), chave);
        else if (cmp > 0) return buscarNo(no.getDir(), chave);
        else              return no;
    }

    public boolean existe(String nome) {
        return buscarNo(raiz, normalizar(nome)) != null;
    }

    // ── REMOÇÃO ───────────────────────────────────────────────────────────────
    public void remover(String nome) {
        raiz = removerRec(raiz, normalizar(nome));
    }

    private NoBST removerRec(NoBST no, String chave) {
        if (no == null) return null;
        int cmp = chave.compareTo(no.getChave());
        if (cmp < 0) {
            no.setEsq(removerRec(no.getEsq(), chave));
        } else if (cmp > 0) {
            no.setDir(removerRec(no.getDir(), chave));
        } else {
            tamanho--;
            if (no.getEsq() == null) return no.getDir();
            if (no.getDir() == null) return no.getEsq();
            NoBST suc    = minimo(no.getDir());
            NoBST novoNo = new NoBST(suc.getChave(), suc.getPoi());
            novoNo.setDir(removerRec(no.getDir(), suc.getChave()));
            novoNo.setEsq(no.getEsq());
            return novoNo;
        }
        return no;
    }

    private NoBST minimo(NoBST no) {
        while (no.getEsq() != null) no = no.getEsq();
        return no;
    }

    // ── PERCURSOS ─────────────────────────────────────────────────────────────
    public ListaLigada inOrder() {
        ListaLigada lista = new ListaLigada();
        inOrderRec(raiz, lista);
        return lista;
    }

    private void inOrderRec(NoBST no, ListaLigada lista) {
        if (no == null) return;
        inOrderRec(no.getEsq(), lista);
        lista.adicionaFim(no.getPoi());
        inOrderRec(no.getDir(), lista);
    }

    public ListaLigada preOrder() {
        ListaLigada lista = new ListaLigada();
        preOrderRec(raiz, lista);
        return lista;
    }

    private void preOrderRec(NoBST no, ListaLigada lista) {
        if (no == null) return;
        lista.adicionaFim(no.getPoi());
        preOrderRec(no.getEsq(), lista);
        preOrderRec(no.getDir(), lista);
    }

    public ListaLigada posOrder() {
        ListaLigada lista = new ListaLigada();
        posOrderRec(raiz, lista);
        return lista;
    }

    private void posOrderRec(NoBST no, ListaLigada lista) {
        if (no == null) return;
        posOrderRec(no.getEsq(), lista);
        posOrderRec(no.getDir(), lista);
        lista.adicionaFim(no.getPoi());
    }

    public ListaLigada buscarPorPrefixo(String prefixo) {
        ListaLigada resultado = new ListaLigada();
        String pref = normalizar(prefixo);
        if (pref.isEmpty()) return resultado;
        buscarPrefixoRec(raiz, pref, resultado);
        return resultado;
    }

    private void buscarPrefixoRec(NoBST no, String pref, ListaLigada res) {
        if (no == null) return;
        int cmp = no.getChave().compareTo(pref);
        if (cmp > 0 || no.getChave().startsWith(pref)) buscarPrefixoRec(no.getEsq(), pref, res);
        if (no.getChave().startsWith(pref)) res.adicionaFim(no.getPoi());
        if (cmp < 0 || no.getChave().startsWith(pref)) buscarPrefixoRec(no.getDir(), pref, res);
    }

    // ── ALTURA ────────────────────────────────────────────────────────────────
    public int altura() { return alturaRec(raiz); }

    private int alturaRec(NoBST no) {
        if (no == null) return 0;
        return 1 + Math.max(alturaRec(no.getEsq()), alturaRec(no.getDir()));
    }

    // ── AUXILIARES ────────────────────────────────────────────────────────────
    private String normalizar(String s) {
        if (s == null) return "";
        String r = s.toLowerCase(java.util.Locale.ROOT);
        r = r.replace("á","a").replace("à","a").replace("â","a").replace("ã","a");
        r = r.replace("é","e").replace("è","e").replace("ê","e");
        r = r.replace("í","i").replace("ì","i").replace("î","i");
        r = r.replace("ó","o").replace("ò","o").replace("ô","o").replace("õ","o");
        r = r.replace("ú","u").replace("ù","u").replace("û","u");
        r = r.replace("ç","c").replace("ñ","n");
        r = r.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        return r;
    }

    public boolean isEmpty()  { return raiz == null; }
    public int getTamanho()   { return tamanho; }
    public NoBST getRaiz()    { return raiz; }
}