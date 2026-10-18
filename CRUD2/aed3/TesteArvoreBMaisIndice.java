import java.util.List;

public class TesteArvoreBMaisIndice {

    public static void main(String[] args) {
        ArvoreBMaisIndice<Integer, String> indice = new ArvoreBMaisIndice<>();
        for (int chave = 2000; chave >= 0; chave--) {
            if (!indice.inserir(chave, "valor-" + chave)) {
                throw new AssertionError("Falha ao inserir chave " + chave);
            }
        }

        if (indice.inserir(1000, "duplicado")) {
            throw new AssertionError("A árvore aceitou uma chave duplicada");
        }
        if (indice.tamanho() != 2001 || !"valor-1000".equals(indice.buscar(1000))
                || indice.buscar(-1) != null || indice.buscar(2001) != null) {
            throw new AssertionError("Busca ou contagem incorreta após divisões da árvore");
        }

        List<String> faixa = indice.buscarIntervalo(150, 355);
        if (faixa.size() != 206 || !"valor-150".equals(faixa.get(0))
                || !"valor-355".equals(faixa.get(faixa.size() - 1))) {
            throw new AssertionError("Busca por intervalo retornou valores incorretos");
        }
        if (indice.listarValores().size() != 2001) {
            throw new AssertionError("Listagem completa perdeu valores");
        }

        ArvoreBMaisIndice<ChaveDataPergunta, Integer> perguntasPorData = new ArvoreBMaisIndice<>();
        perguntasPorData.inserir(new ChaveDataPergunta(100L, 2), 2);
        perguntasPorData.inserir(new ChaveDataPergunta(100L, 1), 1);
        perguntasPorData.inserir(new ChaveDataPergunta(200L, 3), 3);
        List<Integer> perguntasOrdenadas = perguntasPorData.listarValores();
        if (perguntasOrdenadas.size() != 3 || perguntasOrdenadas.get(0) != 3
                || perguntasOrdenadas.get(1) != 1 || perguntasOrdenadas.get(2) != 2) {
            throw new AssertionError("Índice de perguntas não ordenou data decrescente com desempate por ID");
        }

        System.out.println("Teste da árvore B+ OK");
    }
}