import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GerenciadorPerguntas {
    private final ArquivoPergunta arquivoPergunta;
    private final ArvoreBMais indicePorUsuario;
    private final ArvoreBMaisIndice<ChaveDataPergunta, Integer> indicePorData;

    public GerenciadorPerguntas() throws Exception {
        this.arquivoPergunta = new ArquivoPergunta();
        this.indicePorUsuario = new ArvoreBMais();
        this.indicePorData = new ArvoreBMaisIndice<>();
        carregarIndice();
    }

    private void carregarIndice() throws Exception {
        List<Pergunta> perguntas = arquivoPergunta.listarTodas();
        for (Pergunta p : perguntas) {
            if (p != null) {
                indicePorUsuario.inserir(p.idUsuario, p.idPergunta);
                indicePorData.inserir(new ChaveDataPergunta(p.criacao, p.idPergunta), p.idPergunta);
            }
        }
    }

    public int incluirPergunta(int idUsuario, String texto, String palavrasChave) throws Exception {
        Pergunta pergunta = new Pergunta(idUsuario, texto, palavrasChave);
        int id = arquivoPergunta.create(pergunta);
        indicePorUsuario.inserir(idUsuario, id);
        indicePorData.inserir(new ChaveDataPergunta(pergunta.criacao, id), id);
        return id;
    }

    public boolean alterarPergunta(int idPergunta, String novoTexto, String novasPalavrasChave) throws Exception {
        Pergunta existente = arquivoPergunta.read(idPergunta);
        if (existente == null) {
            return false;
        }
        existente.pergunta = novoTexto;
        existente.palavrasChave = novasPalavrasChave;
        existente.alteracao = System.currentTimeMillis();
        return arquivoPergunta.update(existente);
    }

    public boolean arquivarPergunta(int idPergunta) throws Exception {
        Pergunta pergunta = arquivoPergunta.read(idPergunta);
        if (pergunta == null || !pergunta.ativa) {
            return false;
        }
        pergunta.arquivar();
        pergunta.alteracao = System.currentTimeMillis();
        return arquivoPergunta.update(pergunta);
    }

    public List<Pergunta> listarPerguntasDoUsuario(int idUsuario) throws Exception {
        List<Pergunta> resultado = new ArrayList<>();
        List<Integer> ids = indicePorUsuario.buscarPerguntasDoUsuario(idUsuario);
        for (Integer id : ids) {
            Pergunta p = arquivoPergunta.read(id);
            if (p != null) {
                resultado.add(p);
            }
        }
        resultado.sort(Comparator.comparingLong(p -> p.criacao));
        return resultado;
    }

    public List<Pergunta> listarPerguntasAtivasDoUsuario(int idUsuario) throws Exception {
        List<Pergunta> todas = listarPerguntasDoUsuario(idUsuario);
        List<Pergunta> ativas = new ArrayList<>();
        for (Pergunta p : todas) {
            if (p.ativa) {
                ativas.add(p);
            }
        }
        return ativas;
    }

    public List<Pergunta> listarPerguntasPorCriacao() throws Exception {
        List<Pergunta> resultado = new ArrayList<>();
        for (Integer id : indicePorData.listarValores()) {
            Pergunta pergunta = arquivoPergunta.read(id);
            if (pergunta != null) {
                resultado.add(pergunta);
            }
        }
        return resultado;
    }

    public void fechar() throws Exception {
        arquivoPergunta.close();
    }
}
