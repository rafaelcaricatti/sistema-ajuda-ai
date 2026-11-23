import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProcessadorPalavrasChave {

    private static final Set<String> STOP_WORDS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "a", "ao", "aos", "as", "ate",
                    "com", "como", "da", "das", "de", "do", "dos",
                    "e", "em", "entre", "essa", "esse", "esta", "este",
                    "isso", "isto", "mais", "menos", "na", "nas", "no", "nos",
                    "o", "os", "ou", "para", "pela", "pelas", "pelo", "pelos",
                    "por", "porque", "que", "se", "sem", "sob", "sobre",
                    "um", "uma", "uns", "umas",
                    "dois", "duas", "tres", "quatro", "cinco",
                    "seis", "sete", "oito", "nove", "dez",
                    "primeiro", "primeira", "segundo", "segunda",
                    "pergunta", "perguntas", "resposta", "respostas",
                    "ajuda", "duvida", "duvidas"
            ))
    );

    private ProcessadorPalavrasChave() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }

        String normalizado = texto.toLowerCase(Locale.ROOT);
        normalizado = Normalizer.normalize(normalizado, Normalizer.Form.NFD);
        normalizado = normalizado.replaceAll("\\p{M}+", "");
        normalizado = normalizado.replaceAll("[^\\p{L}\\p{Nd}]+", " ");
        normalizado = normalizado.trim().replaceAll("\\s+", " ");

        return normalizado;
    }

    public static List<String> extrairTermos(String texto) {
        String normalizado = normalizar(texto);

        if (normalizado.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> termos = new ArrayList<>();

        for (String termo : normalizado.split(" ")) {
            if (termo.isEmpty() || STOP_WORDS.contains(termo) || termo.matches("\\d+")) {
                continue;
            }

            termos.add(termo);
        }

        return termos;
    }

    public static Map<String, Float> calcularTF(String texto) {
        List<String> termos = extrairTermos(texto);
        Map<String, Float> frequencias = new LinkedHashMap<>();

        if (termos.isEmpty()) {
            return frequencias;
        }

        float frequenciaPorOcorrencia = 1.0f / termos.size();

        for (String termo : termos) {
            frequencias.put(
                    termo,
                    frequencias.getOrDefault(termo, 0.0f) + frequenciaPorOcorrencia
            );
        }

        return frequencias;
    }

    public static boolean ehStopWord(String termo) {
        String normalizado = normalizar(termo);
        return !normalizado.isEmpty() && STOP_WORDS.contains(normalizado);
    }

    public static Set<String> getStopWords() {
        return STOP_WORDS;
    }
}
