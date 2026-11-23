import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MotorBusca {

    private final FonteIndiceInvertido indice;

    public MotorBusca(FonteIndiceInvertido indice) {
        if (indice == null) {
            throw new IllegalArgumentException("Índice não pode ser nulo");
        }
        this.indice = indice;
    }

    public List<ResultadoBusca> buscar(String consulta) throws Exception {
        Set<String> termos = new LinkedHashSet<>(ProcessadorPalavrasChave.extrairTermos(consulta));
        List<ResultadoBusca> resultados = new ArrayList<>();

        if (termos.isEmpty()) {
            return resultados;
        }

        int totalPerguntas = indice.totalEntidades();
        if (totalPerguntas <= 0) {
            return resultados;
        }

        Map<Integer, Float> pesos = new HashMap<>();

        for (String termo : termos) {
            Map<Integer, Float> ocorrencias = indice.buscarTermo(termo);
            if (ocorrencias == null || ocorrencias.isEmpty()) {
                continue;
            }

            float idf = calcularIDF(totalPerguntas, ocorrencias.size());

            for (Map.Entry<Integer, Float> ocorrencia : ocorrencias.entrySet()) {
                float peso = ocorrencia.getValue() * idf;
                pesos.merge(ocorrencia.getKey(), peso, Float::sum);
            }
        }

        for (Map.Entry<Integer, Float> entrada : pesos.entrySet()) {
            resultados.add(new ResultadoBusca(entrada.getKey(), entrada.getValue()));
        }

        resultados.sort((a, b) -> {
            int comparacao = Float.compare(b.getPeso(), a.getPeso());
            return comparacao != 0 ? comparacao : Integer.compare(a.getIdPergunta(), b.getIdPergunta());
        });

        return resultados;
    }

    public List<Integer> buscarIds(String consulta) throws Exception {
        List<Integer> ids = new ArrayList<>();
        for (ResultadoBusca resultado : buscar(consulta)) {
            ids.add(resultado.getIdPergunta());
        }
        return ids;
    }

    public static float calcularIDF(int totalPerguntas, int perguntasComTermo) {
        if (totalPerguntas <= 0 || perguntasComTermo <= 0) {
            return 0.0f;
        }
        return (float) Math.log10((double) totalPerguntas / perguntasComTermo);
    }
}
