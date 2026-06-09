package com.urbanav.urban_nav.heap;

/**
 * MinHeap customizado — estrutura complementar obrigatória (requisito 2.3).
 * Implementa uma fila de prioridade mínima usando array dinâmico.
 * Usado no algoritmo de Dijkstra para seleccionar o nó com menor distância.
 *
 * Cada elemento é um par (idNo, distancia).
 * Propriedade do heap: pai <= filhos em termos de distância.
 *
 * Complexidades:
 *   insert()    → O(log n)
 *   extractMin()→ O(log n)
 *   peek()      → O(1)
 *   isEmpty()   → O(1)
 */
public class MinHeap {

    // Elemento do heap: par (idNo OSM, distância acumulada)
    public static class Entrada {
        public final long idNo;
        public final double distancia;

        public Entrada(long idNo, double distancia) {
            this.idNo = idNo;
            this.distancia = distancia;
        }

        @Override
        public String toString() {
            return "Entrada{id=" + idNo + ", dist=" + String.format("%.2f", distancia) + "}";
        }
    }

    private Entrada[] heap;
    private int tamanho;
    private static final int CAPACIDADE_INICIAL = 1024;

    public MinHeap() {
        this.heap = new Entrada[CAPACIDADE_INICIAL];
        this.tamanho = 0;
    }

    public MinHeap(int capacidadeInicial) {
        this.heap = new Entrada[capacidadeInicial];
        this.tamanho = 0;
    }

    // ── INSERÇÃO ──────────────────────────────────────────────────────────────
    /**
     * Insere um novo par (idNo, distancia) no heap.
     * Após inserir no fim, faz sift-up para restaurar a propriedade do heap.
     * Complexidade: O(log n)
     */
    public void inserir(long idNo, double distancia) {
        if (tamanho == heap.length) {
            expandir();
        }
        heap[tamanho] = new Entrada(idNo, distancia);
        siftUp(tamanho);
        tamanho++;
    }

    // ── EXTRACÇÃO DO MÍNIMO ───────────────────────────────────────────────────
    /**
     * Remove e devolve o elemento com menor distância (raiz do heap).
     * Substitui a raiz pelo último elemento e faz sift-down.
     * Complexidade: O(log n)
     */
    public Entrada extrairMin() {
        if (isEmpty()) {
            throw new IllegalStateException("MinHeap vazio — não é possível extrair");
        }
        Entrada min = heap[0];
        tamanho--;
        if (tamanho > 0) {
            heap[0] = heap[tamanho];
            heap[tamanho] = null;
            siftDown(0);
        } else {
            heap[0] = null;
        }
        return min;
    }

    // ── PEEK ──────────────────────────────────────────────────────────────────
    /**
     * Devolve o mínimo sem remover.
     * Complexidade: O(1)
     */
    public Entrada peek() {
        if (isEmpty()) throw new IllegalStateException("MinHeap vazio");
        return heap[0];
    }

    // ── SIFT-UP ───────────────────────────────────────────────────────────────
    /**
     * Sobe o elemento na posição i até à posição correcta.
     * Compara com o pai e troca enquanto for menor.
     */
    private void siftUp(int i) {
        while (i > 0) {
            int pai = (i - 1) / 2;
            if (heap[i].distancia < heap[pai].distancia) {
                trocar(i, pai);
                i = pai;
            } else {
                break;
            }
        }
    }

    // ── SIFT-DOWN ─────────────────────────────────────────────────────────────
    /**
     * Desce o elemento na posição i até à posição correcta.
     * Compara com os filhos e troca com o menor.
     */
    private void siftDown(int i) {
        while (true) {
            int esq = 2 * i + 1;
            int dir = 2 * i + 2;
            int menor = i;

            if (esq < tamanho && heap[esq].distancia < heap[menor].distancia) {
                menor = esq;
            }
            if (dir < tamanho && heap[dir].distancia < heap[menor].distancia) {
                menor = dir;
            }
            if (menor == i) break;

            trocar(i, menor);
            i = menor;
        }
    }

    // ── AUXILIARES ────────────────────────────────────────────────────────────
    private void trocar(int i, int j) {
        Entrada temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private void expandir() {
        Entrada[] novo = new Entrada[heap.length * 2];
        System.arraycopy(heap, 0, novo, 0, heap.length);
        heap = novo;
    }

    public boolean isEmpty() { return tamanho == 0; }
    public int tamanho()     { return tamanho; }

    // ── PERCURSO (para demonstração) ──────────────────────────────────────────
    /**
     * Imprime o heap nível a nível (percurso por largura implícito pelo array).
     */
    public void imprimir() {
        if (isEmpty()) { System.out.println("MinHeap vazio"); return; }
        System.out.println("MinHeap [" + tamanho + " elementos]:");
        int nivel = 0, inicio = 0, fim = 1;
        while (inicio < tamanho) {
            System.out.print("  Nível " + nivel + ": ");
            for (int i = inicio; i < Math.min(fim, tamanho); i++) {
                System.out.print(heap[i] + " ");
            }
            System.out.println();
            inicio = fim;
            fim = fim * 2 + 1;
            nivel++;
        }
    }

    /**
     * Verifica se a propriedade do heap está correcta (para testes).
     */
    public boolean validar() {
        for (int i = 1; i < tamanho; i++) {
            int pai = (i - 1) / 2;
            if (heap[i].distancia < heap[pai].distancia) return false;
        }
        return true;
    }
}