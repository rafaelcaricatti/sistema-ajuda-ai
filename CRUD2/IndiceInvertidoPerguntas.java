import aed3.ElementoLista;
import aed3.ListaInvertida;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

// Índice das palavras-chave: termo -> (idPergunta, TF). Só perguntas ativas.
public class IndiceInvertidoPerguntas implements FonteIndiceInvertido {

    private static final int ELEMENTOS_POR_BLOCO = 10;

    private final ListaInvertida lista;

    public IndiceInvertidoPerguntas() throws Exception {
        this("./dados/perguntas");
    }

    public IndiceInvertidoPerguntas(String pasta) throws Exception {
        File diretorio = new File(pasta);
        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }

        lista = new ListaInvertida(
                ELEMENTOS_POR_BLOCO,
                pasta + "/indiceInvertido.dicionario.db",
                pasta + "/indiceInvertido.blocos.db"
        );
    }

    public void incluir(Pergunta pergunta) throws Exception {
        if (pergunta == null || !pergunta.ativa) {
            return;
        }

        Map<String, Float> frequencias = ProcessadorPalavrasChave.calcularTF(pergunta.palavrasChave);

        for (Map.Entry<String, Float> termo : frequencias.entrySet()) {
            ElementoLista elemento = new ElementoLista(pergunta.idPergunta, termo.getValue());

            if (!lista.create(termo.getKey(), elemento)) {
                lista.update(termo.getKey(), elemento);
            }
        }

        lista.incrementaEntidades();
    }

    public void remover(Pergunta pergunta) throws Exception {
        if (pergunta == null || !pergunta.ativa) {
            return;
        }

        for (String termo : ProcessadorPalavrasChave.calcularTF(pergunta.palavrasChave).keySet()) {
            lista.delete(termo, pergunta.idPergunta);
        }

        lista.decrementaEntidades();
    }

    public void atualizar(Pergunta antiga, Pergunta nova) throws Exception {
        boolean mudouPalavras = !antiga.palavrasChave.equals(nova.palavrasChave);
        boolean mudouSituacao = antiga.ativa != nova.ativa;

        if (!mudouPalavras && !mudouSituacao) {
            return;
        }

        remover(antiga);
        incluir(nova);
    }

    public boolean vazio() throws Exception {
        return lista.numeroEntidades() == 0;
    }

    @Override
    public Map<Integer, Float> buscarTermo(String termo) throws Exception {
        Map<Integer, Float> ocorrencias = new LinkedHashMap<>();

        for (ElementoLista elemento : lista.read(termo)) {
            ocorrencias.put(elemento.getId(), elemento.getFrequencia());
        }

        return ocorrencias;
    }

    @Override
    public int totalEntidades() throws Exception {
        return lista.numeroEntidades();
    }

    public void imprimir() throws Exception {
        System.out.println("Perguntas indexadas: " + lista.numeroEntidades());
        lista.print();
    }

    public void close() throws Exception {
        lista.close();
    }
}
