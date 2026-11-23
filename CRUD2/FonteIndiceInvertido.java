import java.util.Map;

public interface FonteIndiceInvertido {

    Map<Integer, Float> buscarTermo(String termo) throws Exception;

    int totalEntidades() throws Exception;
}
