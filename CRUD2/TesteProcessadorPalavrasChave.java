import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TesteProcessadorPalavrasChave {

    private static final float TOLERANCIA = 0.0001f;

    public static void main(String[] args) {
        testarNormalizacao();
        testarRemocaoStopWords();
        testarPalavrasChaveDoProjeto();
        testarCalculoTF();
        testarTermoRepetido();
        testarEntradaVazia();

        System.out.println("Teste do processamento de palavras-chave OK");
    }

    private static void testarNormalizacao() {
        String resultado = ProcessadorPalavrasChave.normalizar(
                "Inteligência ARTIFICIAL, Gestão e Pão!"
        );

        conferir(
                "inteligencia artificial gestao e pao".equals(resultado),
                "Normalização de letras, acentos e pontuação"
        );
    }

    private static void testarRemocaoStopWords() {
        List<String> resultado = ProcessadorPalavrasChave.extrairTermos(
                "Introdução à Gestão de Equipes"
        );

        List<String> esperado = Arrays.asList(
                "introducao", "gestao", "equipes"
        );

        conferir(esperado.equals(resultado), "Remoção de stop words");
    }

    private static void testarPalavrasChaveDoProjeto() {
        List<String> resultado = ProcessadorPalavrasChave.extrairTermos(
                "pão;mofado;saúde"
        );

        List<String> esperado = Arrays.asList(
                "pao", "mofado", "saude"
        );

        conferir(
                esperado.equals(resultado),
                "Tratamento de palavras-chave separadas por ponto e vírgula"
        );
    }

    private static void testarCalculoTF() {
        Map<String, Float> tf = ProcessadorPalavrasChave.calcularTF(
                "introdução;inteligência;artificial"
        );

        conferir(proximo(tf.get("introducao"), 1.0f / 3.0f), "TF de introducao");
        conferir(proximo(tf.get("inteligencia"), 1.0f / 3.0f), "TF de inteligencia");
        conferir(proximo(tf.get("artificial"), 1.0f / 3.0f), "TF de artificial");
    }

    private static void testarTermoRepetido() {
        Map<String, Float> tf = ProcessadorPalavrasChave.calcularTF(
                "inteligência;trabalho;meio;inteligência;artificial"
        );

        conferir(proximo(tf.get("inteligencia"), 0.4f), "Acúmulo do TF de termo repetido");
        conferir(proximo(tf.get("trabalho"), 0.2f), "TF de trabalho");
        conferir(proximo(tf.get("meio"), 0.2f), "TF de meio");
        conferir(proximo(tf.get("artificial"), 0.2f), "TF de artificial");
    }

    private static void testarEntradaVazia() {
        conferir(
                ProcessadorPalavrasChave.extrairTermos("").isEmpty(),
                "Entrada vazia"
        );

        conferir(
                ProcessadorPalavrasChave.calcularTF(null).isEmpty(),
                "Entrada nula"
        );
    }

    private static boolean proximo(Float valor, float esperado) {
        return valor != null && Math.abs(valor - esperado) < TOLERANCIA;
    }

    private static void conferir(boolean condicao, String teste) {
        if (!condicao) {
            throw new IllegalStateException("Falhou: " + teste);
        }
    }
}
