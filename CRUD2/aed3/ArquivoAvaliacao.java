import aed3.Arquivo;

import java.util.List;

public class ArquivoAvaliacao {
    private final Arquivo<Avaliacao> arquivo;

    public ArquivoAvaliacao() throws Exception {
        this("avaliacoes");
    }

    public ArquivoAvaliacao(String nome) throws Exception {
        arquivo = new Arquivo<>(nome, Avaliacao.class.getConstructor());
    }

    public int create(Avaliacao avaliacao) throws Exception {
        return arquivo.create(avaliacao);
    }

    public Avaliacao read(int id) throws Exception {
        return arquivo.read(id);
    }

    public boolean update(Avaliacao avaliacao) throws Exception {
        return arquivo.update(avaliacao);
    }

    public boolean delete(int id) throws Exception {
        return arquivo.delete(id);
    }

    public List<Avaliacao> listarTodas() throws Exception {
        return arquivo.listarTodos();
    }

    public void close() throws Exception {
        arquivo.close();
    }
}