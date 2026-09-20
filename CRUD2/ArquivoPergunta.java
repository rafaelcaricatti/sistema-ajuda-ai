import aed3.Arquivo;

import java.util.ArrayList;
import java.util.List;

public class ArquivoPergunta {
    private final Arquivo<Pergunta> arquivo;

    public ArquivoPergunta() throws Exception {
        this.arquivo = new Arquivo<>("perguntas", Pergunta.class.getConstructor());
    }

    public int create(Pergunta pergunta) throws Exception {
        return arquivo.create(pergunta);
    }

    public Pergunta read(int id) throws Exception {
        return arquivo.read(id);
    }

    public boolean update(Pergunta pergunta) throws Exception {
        return arquivo.update(pergunta);
    }

    public boolean delete(int id) throws Exception {
        return arquivo.delete(id);
    }

    public List<Pergunta> listarTodas() throws Exception {
        List<Pergunta> todos = new ArrayList<>();
        int ultimoId = 0;
        while (true) {
            Pergunta p = arquivo.read(++ultimoId);
            if (p == null) {
                break;
            }
            todos.add(p);
        }
        return todos;
    }

    public void close() throws Exception {
        arquivo.close();
    }
}
