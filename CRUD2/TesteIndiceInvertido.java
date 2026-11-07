import aed3.ElementoLista;
import aed3.ListaInvertida;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// Usa uma pasta temporária para não mexer em ./dados
public class TesteIndiceInvertido {

    private static final float TOLERANCIA = 0.0001f;

    public static void main(String[] args) throws Exception {
        File pasta = Files.createTempDirectory("teste-indice-invertido").toFile();

        try {
            testarListaInvertida(pasta.getPath() + "/lista");
            testarPersistenciaListaInvertida(pasta.getPath() + "/persistencia");
            testarIndicePerguntas(pasta.getPath() + "/indice");
        } finally {
            apagar(pasta);
        }

        System.out.println("Teste do índice invertido OK");
    }

    private static void testarListaInvertida(String pasta) throws Exception {
        new File(pasta).mkdirs();
        // blocos de 2 para testar o encadeamento
        ListaInvertida lista = new ListaInvertida(2, pasta + "/dic.db", pasta + "/blocos.db");

        // create
        conferir(lista.create("java", new ElementoLista(3, 0.5f)), "create de um termo novo");
        conferir(lista.create("java", new ElementoLista(1, 0.25f)), "create de outro id no mesmo termo");
        conferir(lista.create("java", new ElementoLista(7, 1.0f)), "create que passa para o segundo bloco");
        conferir(!lista.create("java", new ElementoLista(3, 0.9f)), "create recusa id repetido");
        conferir(lista.create("arvore", new ElementoLista(3, 0.5f)), "create em outro termo");

        // read
        ElementoLista[] java = lista.read("java");
        conferir(java.length == 3, "read devolve todos os elementos do termo");
        conferir(java[0].getId() == 1 && java[1].getId() == 3 && java[2].getId() == 7, "read ordenado por id");
        conferir(proximo(lista.read("java", 7).getFrequencia(), 1.0f), "read de um id no segundo bloco");
        conferir(lista.read("hash").length == 0, "read de termo inexistente");
        conferir(lista.read("java", 99) == null, "read de id inexistente");

        // update
        conferir(lista.update("java", new ElementoLista(3, 0.75f)), "update de um elemento");
        conferir(proximo(lista.read("java", 3).getFrequencia(), 0.75f), "update alterou a frequência");
        conferir(!lista.update("java", new ElementoLista(99, 0.1f)), "update de id inexistente");
        conferir(!lista.update("hash", new ElementoLista(3, 0.1f)), "update de termo inexistente");

        // delete
        conferir(lista.delete("java", 1), "delete de um elemento");
        conferir(lista.read("java").length == 2, "delete retirou o elemento");
        conferir(lista.read("java", 1) == null, "elemento removido não é mais lido");
        conferir(lista.delete("java", 7), "delete no segundo bloco");
        conferir(!lista.delete("java", 7), "delete repetido falha");
        conferir(!lista.delete("hash", 3), "delete de termo inexistente");
        conferir(lista.read("arvore").length == 1, "delete não afeta outros termos");

        // contador de entidades
        lista.incrementaEntidades();
        lista.incrementaEntidades();
        lista.decrementaEntidades();
        conferir(lista.numeroEntidades() == 1, "contador de entidades");

        lista.close();
    }

    private static void testarPersistenciaListaInvertida(String pasta) throws Exception {
        new File(pasta).mkdirs();
        ListaInvertida lista = new ListaInvertida(2, pasta + "/dic.db", pasta + "/blocos.db");
        for (int id = 1; id <= 5; id++) {
            lista.create("termo", new ElementoLista(id, id / 10.0f));
            lista.incrementaEntidades();
        }
        lista.close();

        ListaInvertida reaberta = new ListaInvertida(2, pasta + "/dic.db", pasta + "/blocos.db");
        conferir(reaberta.read("termo").length == 5, "elementos continuam após reabrir os arquivos");
        conferir(proximo(reaberta.read("termo", 4).getFrequencia(), 0.4f), "frequência continua após reabrir");
        conferir(reaberta.numeroEntidades() == 5, "contador continua após reabrir");
        reaberta.close();
    }

    private static void testarIndicePerguntas(String pasta) throws Exception {
        IndiceInvertidoPerguntas indice = new IndiceInvertidoPerguntas(pasta);
        indice.incluir(pergunta(1, "Introdução à Inteligência Artificial"));
        indice.incluir(pergunta(2, "Inteligência Emocional para Gestores"));
        indice.incluir(pergunta(3, "Inteligência no Trabalho por Meio da Inteligência Artificial"));
        indice.incluir(pergunta(4, "Introdução à Gestão de Equipes"));

        conferir(indice.totalEntidades() == 4, "total de perguntas indexadas");
        conferir(proximo(indice.buscarTermo("inteligencia").get(3), 0.4f), "TF de inteligencia na pergunta 3");
        conferir(indice.buscarTermo("inteligencia").size() == 3, "lista invertida de inteligencia");

        // exemplo do enunciado
        MotorBusca motor = new MotorBusca(indice);
        List<ResultadoBusca> resultados = motor.buscar("Inteligência Artificial");
        conferir(Arrays.asList(1, 3, 2).equals(motor.buscarIds("Inteligência Artificial")), "ranking do enunciado");
        conferir(proximo(resultados.get(0).getPeso(), 0.142f, 0.001f), "peso da pergunta 1");

        // alteração das palavras-chave
        Pergunta antiga = pergunta(2, "Inteligência Emocional para Gestores");
        Pergunta nova = pergunta(2, "Liderança Emocional");
        indice.atualizar(antiga, nova);
        conferir(!indice.buscarTermo("inteligencia").containsKey(2), "termo antigo saiu do índice");
        conferir(proximo(indice.buscarTermo("emocional").get(2), 0.5f), "TF recalculado após alterar");
        conferir(indice.buscarTermo("lideranca").containsKey(2), "termo novo entrou no índice");
        conferir(indice.totalEntidades() == 4, "alterar não muda o total de perguntas");

        // arquivamento
        Pergunta arquivada = pergunta(2, "Liderança Emocional");
        arquivada.arquivar();
        indice.atualizar(nova, arquivada);
        conferir(indice.buscarTermo("lideranca").isEmpty(), "pergunta arquivada sai do índice");
        conferir(indice.totalEntidades() == 3, "arquivar diminui o total de perguntas");

        // remoção
        indice.remover(pergunta(4, "Introdução à Gestão de Equipes"));
        conferir(indice.buscarTermo("equipes").isEmpty(), "remoção tira os termos da pergunta");
        conferir(indice.buscarTermo("introducao").size() == 1, "remoção mantém as outras perguntas");
        conferir(indice.totalEntidades() == 2, "remoção diminui o total de perguntas");
        indice.close();

        IndiceInvertidoPerguntas reaberto = new IndiceInvertidoPerguntas(pasta);
        conferir(reaberto.totalEntidades() == 2, "índice persistente após reabrir");
        conferir(Arrays.asList(1, 3).equals(new MotorBusca(reaberto).buscarIds("artificial")), "busca após reabrir");
        reaberto.close();
    }

    private static Pergunta pergunta(int id, String palavrasChave) {
        return new Pergunta(id, 1, "texto da pergunta " + id, palavrasChave);
    }

    private static void apagar(File arquivo) {
        File[] filhos = arquivo.listFiles();
        if (filhos != null) {
            for (File filho : filhos) {
                apagar(filho);
            }
        }
        arquivo.delete();
    }

    private static boolean proximo(Float valor, float esperado) {
        return proximo(valor, esperado, TOLERANCIA);
    }

    private static boolean proximo(Float valor, float esperado, float tolerancia) {
        return valor != null && Math.abs(valor - esperado) < tolerancia;
    }

    private static void conferir(boolean condicao, String teste) {
        if (!condicao) {
            throw new IllegalStateException("Falhou: " + teste);
        }
    }
}
