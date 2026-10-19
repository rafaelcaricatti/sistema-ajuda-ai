import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GerenciadorPerguntas {

    private final ArquivoPergunta arquivoPergunta;

    public GerenciadorPerguntas() throws Exception {
        this.arquivoPergunta = new ArquivoPergunta();
    }

    public int incluirPergunta(int idUsuario, String texto, String palavrasChave) throws Exception {
        Pergunta pergunta = new Pergunta(idUsuario, texto, palavrasChave);
        return arquivoPergunta.create(pergunta);
    }

    public boolean alterarPergunta(int idUsuario, int idPergunta,
                                    String novoTexto, String novasPalavrasChave) throws Exception {
        Pergunta existente = arquivoPergunta.read(idPergunta);

        if (existente == null || existente.idUsuario != idUsuario || !existente.ativa) {
            return false;
        }

        existente.pergunta = novoTexto;
        existente.palavrasChave = novasPalavrasChave;
        existente.alteracao = System.currentTimeMillis();

        return arquivoPergunta.update(existente);
    }

    public boolean arquivarPergunta(int idUsuario, int idPergunta) throws Exception {
        Pergunta pergunta = arquivoPergunta.read(idPergunta);

        if (pergunta == null || pergunta.idUsuario != idUsuario || !pergunta.ativa) {
            return false;
        }

        pergunta.arquivar();
        pergunta.alteracao = System.currentTimeMillis();

        return arquivoPergunta.update(pergunta);
    }

    public List<Pergunta> listarPerguntasDoUsuario(int idUsuario) throws Exception {
        List<Pergunta> resultado = new ArrayList<>(
                Arrays.asList(arquivoPergunta.readAllByUsuario(idUsuario))
        );

        resultado.sort(Comparator.comparingLong(p -> p.criacao));

        return resultado;
    }

    public List<Pergunta> listarPerguntasAtivasDoUsuario(int idUsuario) throws Exception {
        List<Pergunta> ativas = new ArrayList<>();

        for (Pergunta pergunta : listarPerguntasDoUsuario(idUsuario)) {
            if (pergunta.ativa) {
                ativas.add(pergunta);
            }
        }

        return ativas;
    }

    public Pergunta buscarPergunta(int idPergunta) throws Exception {
        return arquivoPergunta.read(idPergunta);
    }

    public List<Pergunta> listarPerguntasMaisRecentes() throws Exception {
        return new ArrayList<>(
                Arrays.asList(arquivoPergunta.readAllByData())
        );
    }

    public void fechar() throws Exception {
        arquivoPergunta.close();
    }
}
