import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArvoreBMais {
    private static final int ORDEM = 4;
    private No raiz;

    private static class No {
        boolean folha;
        List<ParUsuarioPergunta> chaves;
        List<No> filhos;

        No(boolean folha) {
            this.folha = folha;
            this.chaves = new ArrayList<>();
            this.filhos = new ArrayList<>();
        }
    }

    public ArvoreBMais() {
        this.raiz = new No(true);
    }

    public void inserir(int idUsuario, int idPergunta) {
        ParUsuarioPergunta novo = new ParUsuarioPergunta(idUsuario, idPergunta);
        if (raiz == null) {
            raiz = new No(true);
        }

        if (raiz.folha) {
            inserirNaFolha(raiz, novo);
            if (raiz.chaves.size() > ORDEM) {
                dividirRaiz();
            }
            return;
        }

        inserirRecursivo(raiz, novo);
        if (raiz.chaves.size() > ORDEM) {
            dividirRaiz();
        }
    }

    private void inserirNaFolha(No no, ParUsuarioPergunta item) {
        no.chaves.add(item);
        Collections.sort(no.chaves, (a, b) -> {
            int cmp = Integer.compare(a.getIdUsuario(), b.getIdUsuario());
            if (cmp != 0) return cmp;
            return Integer.compare(a.getIdPergunta(), b.getIdPergunta());
        });
    }

    private void inserirRecursivo(No no, ParUsuarioPergunta item) {
        if (no.folha) {
            inserirNaFolha(no, item);
            if (no.chaves.size() > ORDEM) {
                dividirNo(no);
            }
            return;
        }

        int indice = 0;
        while (indice < no.filhos.size() && indice < no.chaves.size() && item.compareTo(no.chaves.get(indice)) >= 0) {
            indice++;
        }

        if (indice >= no.filhos.size()) {
            indice = no.filhos.size() - 1;
        }

        inserirRecursivo(no.filhos.get(indice), item);
    }

    private void dividirRaiz() {
        if (raiz.folha) {
            No novoDireita = new No(true);
            int meio = raiz.chaves.size() / 2;
            for (int i = meio; i < raiz.chaves.size(); i++) {
                novoDireita.chaves.add(raiz.chaves.get(i));
            }
            for (int i = raiz.chaves.size() - 1; i >= meio; i--) {
                raiz.chaves.remove(i);
            }

            No novaRaiz = new No(false);
            novaRaiz.chaves.add(novoDireita.chaves.get(0));
            novaRaiz.filhos.add(raiz);
            novaRaiz.filhos.add(novoDireita);
            raiz = novaRaiz;
            return;
        }

        dividirNo(raiz);
    }

    private void dividirNo(No no) {
        if (no.chaves.size() <= ORDEM) {
            return;
        }

        No esquerda = new No(true);
        No direita = new No(true);
        int meio = no.chaves.size() / 2;

        for (int i = 0; i < meio; i++) {
            esquerda.chaves.add(no.chaves.get(i));
        }
        for (int i = meio; i < no.chaves.size(); i++) {
            direita.chaves.add(no.chaves.get(i));
        }

        if (no.filhos.size() > 0) {
            for (int i = 0; i < meio; i++) {
                esquerda.filhos.add(no.filhos.get(i));
            }
            for (int i = meio; i < no.filhos.size(); i++) {
                direita.filhos.add(no.filhos.get(i));
            }
        }

        no.chaves.clear();
        no.filhos.clear();
        no.folha = false;
        no.chaves.add(direita.chaves.get(0));
        no.filhos.add(esquerda);
        no.filhos.add(direita);
    }

    public List<Integer> buscarPerguntasDoUsuario(int idUsuario) {
        List<Integer> resultado = new ArrayList<>();
        percorrer(raiz, idUsuario, resultado);
        Collections.sort(resultado);
        return resultado;
    }

    private void percorrer(No no, int idUsuario, List<Integer> resultado) {
        if (no == null) {
            return;
        }

        if (no.folha) {
            for (ParUsuarioPergunta item : no.chaves) {
                if (item.getIdUsuario() == idUsuario) {
                    resultado.add(item.getIdPergunta());
                }
            }
            return;
        }

        for (int i = 0; i < no.chaves.size(); i++) {
            ParUsuarioPergunta atual = no.chaves.get(i);
            if (atual.getIdUsuario() > idUsuario) {
                percorrer(no.filhos.get(i), idUsuario, resultado);
                return;
            }
        }
        percorrer(no.filhos.get(no.filhos.size() - 1), idUsuario, resultado);
    }

    public boolean remover(int idUsuario, int idPergunta) {
        return removerRecursivo(raiz, idUsuario, idPergunta);
    }

    private boolean removerRecursivo(No no, int idUsuario, int idPergunta) {
        if (no == null) {
            return false;
        }

        for (int i = 0; i < no.chaves.size(); i++) {
            ParUsuarioPergunta item = no.chaves.get(i);
            if (item.getIdUsuario() == idUsuario && item.getIdPergunta() == idPergunta) {
                no.chaves.remove(i);
                return true;
            }
        }

        if (no.folha) {
            return false;
        }

        for (int i = 0; i < no.filhos.size(); i++) {
            if (removerRecursivo(no.filhos.get(i), idUsuario, idPergunta)) {
                return true;
            }
        }
        return false;
    }

    public boolean existe(int idUsuario, int idPergunta) {
        return buscarPerguntasDoUsuario(idUsuario).contains(idPergunta);
    }
}
