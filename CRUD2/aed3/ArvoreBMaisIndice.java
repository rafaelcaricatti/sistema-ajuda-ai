import java.util.ArrayList;
import java.util.List;

public class ArvoreBMaisIndice<K extends Comparable<K>, V> {
    private static final int MAX_CHAVES = 4;
    private No<K, V> raiz = new No<>(true);
    private int tamanho;

    private static class No<K, V> {
        private boolean folha;
        private final List<K> chaves = new ArrayList<>();
        private final List<V> valores = new ArrayList<>();
        private final List<No<K, V>> filhos = new ArrayList<>();

        private No(boolean folha) {
            this.folha = folha;
        }
    }

    private static class ResultadoInsercao<K, V> {
        private final boolean inserido;
        private final K chavePromovida;
        private final No<K, V> filhoDireito;

        private ResultadoInsercao(boolean inserido, K chavePromovida, No<K, V> filhoDireito) {
            this.inserido = inserido;
            this.chavePromovida = chavePromovida;
            this.filhoDireito = filhoDireito;
        }
    }

    public boolean inserir(K chave, V valor) {
        if (chave == null) {
            throw new IllegalArgumentException("A chave não pode ser nula");
        }

        ResultadoInsercao<K, V> resultado = inserir(raiz, chave, valor);
        if (!resultado.inserido) {
            return false;
        }
        tamanho++;

        if (resultado.filhoDireito != null) {
            No<K, V> novaRaiz = new No<>(false);
            novaRaiz.chaves.add(resultado.chavePromovida);
            novaRaiz.filhos.add(raiz);
            novaRaiz.filhos.add(resultado.filhoDireito);
            raiz = novaRaiz;
        }
        return true;
    }

    private ResultadoInsercao<K, V> inserir(No<K, V> no, K chave, V valor) {
        if (no.folha) {
            int indice = lowerBound(no.chaves, chave);
            if (indice < no.chaves.size() && no.chaves.get(indice).compareTo(chave) == 0) {
                return new ResultadoInsercao<>(false, null, null);
            }

            no.chaves.add(indice, chave);
            no.valores.add(indice, valor);
            if (no.chaves.size() <= MAX_CHAVES) {
                return new ResultadoInsercao<>(true, null, null);
            }
            return dividirFolha(no);
        }

        int indiceFilho = 0;
        while (indiceFilho < no.chaves.size()
                && chave.compareTo(no.chaves.get(indiceFilho)) >= 0) {
            indiceFilho++;
        }

        ResultadoInsercao<K, V> resultado = inserir(no.filhos.get(indiceFilho), chave, valor);
        if (!resultado.inserido || resultado.filhoDireito == null) {
            return resultado;
        }

        no.chaves.add(indiceFilho, resultado.chavePromovida);
        no.filhos.add(indiceFilho + 1, resultado.filhoDireito);
        if (no.chaves.size() <= MAX_CHAVES) {
            return new ResultadoInsercao<>(true, null, null);
        }
        return dividirInterno(no);
    }

    private ResultadoInsercao<K, V> dividirFolha(No<K, V> folha) {
        int meio = folha.chaves.size() / 2;
        No<K, V> direita = new No<>(true);
        direita.chaves.addAll(folha.chaves.subList(meio, folha.chaves.size()));
        direita.valores.addAll(folha.valores.subList(meio, folha.valores.size()));
        folha.chaves.subList(meio, folha.chaves.size()).clear();
        folha.valores.subList(meio, folha.valores.size()).clear();
        return new ResultadoInsercao<>(true, direita.chaves.get(0), direita);
    }

    private ResultadoInsercao<K, V> dividirInterno(No<K, V> interno) {
        int meio = interno.chaves.size() / 2;
        K promovida = interno.chaves.get(meio);
        No<K, V> direita = new No<>(false);

        direita.chaves.addAll(interno.chaves.subList(meio + 1, interno.chaves.size()));
        direita.filhos.addAll(interno.filhos.subList(meio + 1, interno.filhos.size()));
        interno.chaves.subList(meio, interno.chaves.size()).clear();
        interno.filhos.subList(meio + 1, interno.filhos.size()).clear();
        return new ResultadoInsercao<>(true, promovida, direita);
    }

    public V buscar(K chave) {
        if (chave == null) {
            return null;
        }
        No<K, V> no = raiz;
        while (!no.folha) {
            int indiceFilho = 0;
            while (indiceFilho < no.chaves.size()
                    && chave.compareTo(no.chaves.get(indiceFilho)) >= 0) {
                indiceFilho++;
            }
            no = no.filhos.get(indiceFilho);
        }

        int indice = lowerBound(no.chaves, chave);
        if (indice < no.chaves.size() && no.chaves.get(indice).compareTo(chave) == 0) {
            return no.valores.get(indice);
        }
        return null;
    }

    public List<V> buscarIntervalo(K minimo, K maximo) {
        List<V> resultado = new ArrayList<>();
        if (minimo == null || maximo == null || minimo.compareTo(maximo) > 0) {
            return resultado;
        }
        buscarIntervalo(raiz, minimo, maximo, resultado);
        return resultado;
    }

    private void buscarIntervalo(No<K, V> no, K minimo, K maximo, List<V> resultado) {
        if (no.folha) {
            int indice = lowerBound(no.chaves, minimo);
            while (indice < no.chaves.size() && no.chaves.get(indice).compareTo(maximo) <= 0) {
                resultado.add(no.valores.get(indice));
                indice++;
            }
            return;
        }

        int indiceFilho = 0;
        while (indiceFilho < no.chaves.size()
                && minimo.compareTo(no.chaves.get(indiceFilho)) >= 0) {
            indiceFilho++;
        }

        while (indiceFilho < no.filhos.size()) {
            buscarIntervalo(no.filhos.get(indiceFilho), minimo, maximo, resultado);
            if (indiceFilho == no.chaves.size()
                    || maximo.compareTo(no.chaves.get(indiceFilho)) < 0) {
                break;
            }
            indiceFilho++;
        }
    }

    public List<V> listarValores() {
        List<V> resultado = new ArrayList<>(tamanho);
        listarValores(raiz, resultado);
        return resultado;
    }

    private void listarValores(No<K, V> no, List<V> resultado) {
        if (no.folha) {
            resultado.addAll(no.valores);
            return;
        }
        for (No<K, V> filho : no.filhos) {
            listarValores(filho, resultado);
        }
    }

    public int tamanho() {
        return tamanho;
    }

    private int lowerBound(List<K> chaves, K procurada) {
        int inicio = 0;
        int fim = chaves.size();
        while (inicio < fim) {
            int meio = (inicio + fim) >>> 1;
            if (chaves.get(meio).compareTo(procurada) < 0) {
                inicio = meio + 1;
            } else {
                fim = meio;
            }
        }
        return inicio;
    }
}