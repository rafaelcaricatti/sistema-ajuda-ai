import aed3.Arquivo;

import java.util.List;

public class ArquivoResposta {
    private final Arquivo<Resposta> arquivo;

    public ArquivoResposta() throws Exception {
        this("respostas");
    }

    public ArquivoResposta(String nome) throws Exception {
        arquivo = new Arquivo<>(nome, Resposta.class.getConstructor());
    }

    public int create(Resposta resposta) throws Exception {
        return arquivo.create(resposta);
    }

    public Resposta read(int id) throws Exception {
        return arquivo.read(id);
    }

    public boolean update(Resposta resposta) throws Exception {
        return arquivo.update(resposta);
    }

    public boolean delete(int id) throws Exception {
        return arquivo.delete(id);
    }

    public List<Resposta> listarTodas() throws Exception {
        return arquivo.listarTodos();
    }

    public void close() throws Exception {
        arquivo.close();
    }
}