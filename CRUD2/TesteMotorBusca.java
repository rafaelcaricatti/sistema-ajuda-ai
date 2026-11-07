import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TesteMotorBusca {

    private static final float TOLERANCIA = 0.0001f;

    public static void main(String[] args) throws Exception {
        testarCalculoIDF();
        testarBuscaUmaPalavra();
        testarBuscaVariasPalavras();
        testarOrdenacaoPorTF();
        testarNormalizacaoDaConsulta();
        testarTermoInexistente();
        testarConsultaSomenteStopWords();
        testarTermoRepetidoNaConsulta();
        testarIndiceVazio();

        System.out.println("Teste do motor de busca OK");
    }

    private static class IndiceEmMemoria implements FonteIndiceInvertido {

        private final Map<String, Map<Integer, Float>> listas = new HashMap<>();
        private int totalEntidades;

        void incluir(int idPergunta, String palavrasChave) {
            for (Map.Entry<String, Float> entrada : ProcessadorPalavrasChave.calcularTF(palavrasChave).entrySet()) {
                listas.computeIfAbsent(entrada.getKey(), k -> new HashMap<>())
                        .put(idPergunta, entrada.getValue());
            }
            totalEntidades++;
        }

        @Override
        public Map<Integer, Float> buscarTermo(String termo) {
            return listas.getOrDefault(termo, new HashMap<>());
        }

        @Override
        public int totalEntidades() {
            return totalEntidades;
        }
    }

    private static IndiceEmMemoria criarIndice() {
        IndiceEmMemoria indice = new IndiceEmMemoria();
        indice.incluir(1, "pão;mofado;saúde");
        indice.incluir(2, "inteligência;artificial;trabalho");
        indice.incluir(3, "inteligência;inteligência;artificial");
        indice.incluir(4, "saúde;mental;trabalho");
        indice.incluir(5, "receita;pão;caseiro");
        return indice;
    }

    private static void testarCalculoIDF() {
        conferir(proximo(MotorBusca.calcularIDF(5, 2), (float) Math.log10(2.5)), "IDF de termo em 2 de 5 perguntas");
        conferir(proximo(MotorBusca.calcularIDF(5, 5), 0.0f), "IDF de termo presente em todas as perguntas");
        conferir(proximo(MotorBusca.calcularIDF(0, 0), 0.0f), "IDF sem perguntas");
    }

    private static void testarBuscaUmaPalavra() throws Exception {
        MotorBusca motor = new MotorBusca(criarIndice());
        List<ResultadoBusca> resultados = motor.buscar("pão");

        float esperado = (1.0f / 3.0f) * (float) Math.log10(5.0 / 2.0);

        conferir(resultados.size() == 2, "Quantidade de resultados com uma palavra");
        conferir(Arrays.asList(1, 5).equals(motor.buscarIds("pão")), "IDs com uma palavra");
        conferir(proximo(resultados.get(0).getPeso(), esperado), "Peso TF x IDF com uma palavra");
    }

    private static void testarBuscaVariasPalavras() throws Exception {
        MotorBusca motor = new MotorBusca(criarIndice());
        List<ResultadoBusca> resultados = motor.buscar("saúde trabalho");

        float pesoTermo = (1.0f / 3.0f) * (float) Math.log10(5.0 / 2.0);

        conferir(Arrays.asList(4, 1, 2).equals(motor.buscarIds("saúde trabalho")), "Ranking com várias palavras");
        conferir(proximo(resultados.get(0).getPeso(), 2 * pesoTermo), "Soma dos pesos da pergunta com os dois termos");
        conferir(proximo(resultados.get(1).getPeso(), pesoTermo), "Peso da pergunta com um termo");
    }

    private static void testarOrdenacaoPorTF() throws Exception {
        MotorBusca motor = new MotorBusca(criarIndice());
        List<ResultadoBusca> resultados = motor.buscar("inteligência");

        float idf = (float) Math.log10(5.0 / 2.0);

        conferir(Arrays.asList(3, 2).equals(motor.buscarIds("inteligência")), "Ordenação pelo maior TF");
        conferir(proximo(resultados.get(0).getPeso(), (2.0f / 3.0f) * idf), "Peso com TF maior");
        conferir(proximo(resultados.get(1).getPeso(), (1.0f / 3.0f) * idf), "Peso com TF menor");
    }

    private static void testarNormalizacaoDaConsulta() throws Exception {
        MotorBusca motor = new MotorBusca(criarIndice());

        conferir(
                motor.buscarIds("SAÚDE, Trabalho!").equals(motor.buscarIds("saude trabalho")),
                "Consulta normalizada igual à indexação"
        );
    }

    private static void testarTermoInexistente() throws Exception {
        MotorBusca motor = new MotorBusca(criarIndice());

        conferir(motor.buscar("astronomia").isEmpty(), "Termo inexistente");
        conferir(motor.buscarIds("pão astronomia").equals(motor.buscarIds("pão")), "Termo inexistente misturado");
    }

    private static void testarConsultaSomenteStopWords() throws Exception {
        MotorBusca motor = new MotorBusca(criarIndice());

        conferir(motor.buscar("de para com").isEmpty(), "Consulta só com stop words");
        conferir(motor.buscar("").isEmpty(), "Consulta vazia");
        conferir(motor.buscar(null).isEmpty(), "Consulta nula");
    }

    private static void testarTermoRepetidoNaConsulta() throws Exception {
        MotorBusca motor = new MotorBusca(criarIndice());

        List<ResultadoBusca> simples = motor.buscar("pão");
        List<ResultadoBusca> repetida = motor.buscar("pão pão");

        conferir(proximo(simples.get(0).getPeso(), repetida.get(0).getPeso()), "Termo repetido na consulta conta uma vez");
    }

    private static void testarIndiceVazio() throws Exception {
        MotorBusca motor = new MotorBusca(new IndiceEmMemoria());

        conferir(motor.buscar("pão").isEmpty(), "Índice vazio");
    }

    private static boolean proximo(float valor, float esperado) {
        return Math.abs(valor - esperado) < TOLERANCIA;
    }

    private static void conferir(boolean condicao, String teste) {
        if (!condicao) {
            throw new IllegalStateException("Falhou: " + teste);
        }
    }
}
