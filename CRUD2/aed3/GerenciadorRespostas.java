import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GerenciadorRespostas {
    private final ArquivoResposta arquivoResposta;
    private final ArquivoAvaliacao arquivoAvaliacao;
    private final ArvoreBMaisIndice<ChavePar, Integer> indicePorPergunta;
    private final ArvoreBMaisIndice<ChavePar, Integer> indicePorUsuario;
    private final ArvoreBMaisIndice<ChavePar, Integer> indiceAvaliacao;

    public GerenciadorRespostas() throws Exception {
        this("respostas", "avaliacoes");
    }

    public GerenciadorRespostas(String nomeArquivoRespostas, String nomeArquivoAvaliacoes) throws Exception {
        arquivoResposta = new ArquivoResposta(nomeArquivoRespostas);
        arquivoAvaliacao = new ArquivoAvaliacao(nomeArquivoAvaliacoes);
        indicePorPergunta = new ArvoreBMaisIndice<>();
        indicePorUsuario = new ArvoreBMaisIndice<>();
        indiceAvaliacao = new ArvoreBMaisIndice<>();
        carregarIndices();
    }

    private void carregarIndices() throws Exception {
        List<Resposta> respostas = arquivoResposta.listarTodas();
        Map<Integer, Integer> notas = new HashMap<>();
        Map<Integer, Integer> quantidades = new HashMap<>();

        for (Resposta resposta : respostas) {
            indicePorPergunta.inserir(new ChavePar(resposta.idPergunta, resposta.idResposta), resposta.idResposta);
            indicePorUsuario.inserir(new ChavePar(resposta.idUsuario, resposta.idResposta), resposta.idResposta);
        }

        for (Avaliacao avaliacao : arquivoAvaliacao.listarTodas()) {
            ChavePar chave = new ChavePar(avaliacao.idUsuario, avaliacao.idResposta);
            if (!indiceAvaliacao.inserir(chave, avaliacao.idAvaliacao)) {
                continue;
            }

            Resposta resposta = arquivoResposta.read(avaliacao.idResposta);
            if (resposta != null) {
                int incremento = avaliacao.avaliacao ? 1 : -1;
                notas.put(resposta.idResposta, notas.getOrDefault(resposta.idResposta, 0) + incremento);
                quantidades.put(resposta.idResposta, quantidades.getOrDefault(resposta.idResposta, 0) + 1);
            }
        }

        for (Resposta resposta : respostas) {
            int nota = notas.getOrDefault(resposta.idResposta, 0);
            int quantidade = quantidades.getOrDefault(resposta.idResposta, 0);
            if (nota < Short.MIN_VALUE || nota > Short.MAX_VALUE || quantidade > Short.MAX_VALUE) {
                throw new IllegalStateException("As avaliações excedem a capacidade dos campos short");
            }
            if (resposta.nota != nota || resposta.avaliacoes != quantidade) {
                resposta.nota = (short) nota;
                resposta.avaliacoes = (short) quantidade;
                arquivoResposta.update(resposta);
            }
        }
    }

    public int incluirResposta(int idPergunta, int idUsuario, String texto) throws Exception {
        if (idPergunta <= 0 || idUsuario <= 0 || texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Pergunta, usuário e texto da resposta são obrigatórios");
        }

        Resposta resposta = new Resposta(idPergunta, idUsuario, texto);
        int id = arquivoResposta.create(resposta);
        indicePorPergunta.inserir(new ChavePar(idPergunta, id), id);
        indicePorUsuario.inserir(new ChavePar(idUsuario, id), id);
        return id;
    }

    public Resposta buscarResposta(int idResposta) throws Exception {
        return arquivoResposta.read(idResposta);
    }

    public List<Resposta> listarRespostasDaPergunta(int idPergunta) throws Exception {
        List<Integer> ids = indicePorPergunta.buscarIntervalo(
                new ChavePar(idPergunta, Integer.MIN_VALUE),
                new ChavePar(idPergunta, Integer.MAX_VALUE));
        return carregarRespostasAtivas(ids);
    }

    public List<Resposta> listarRespostasDoUsuario(int idUsuario) throws Exception {
        List<Integer> ids = indicePorUsuario.buscarIntervalo(
                new ChavePar(idUsuario, Integer.MIN_VALUE),
                new ChavePar(idUsuario, Integer.MAX_VALUE));
        return carregarRespostasAtivas(ids);
    }

    private List<Resposta> carregarRespostasAtivas(List<Integer> ids) throws Exception {
        List<Resposta> resultado = new ArrayList<>();
        for (Integer id : ids) {
            Resposta resposta = arquivoResposta.read(id);
            if (resposta != null && resposta.ativa) {
                resultado.add(resposta);
            }
        }
        return resultado;
    }

    public boolean arquivarResposta(int idResposta) throws Exception {
        Resposta resposta = arquivoResposta.read(idResposta);
        if (resposta == null || !resposta.ativa) {
            return false;
        }
        resposta.ativa = false;
        return arquivoResposta.update(resposta);
    }

    public synchronized boolean avaliarResposta(int idUsuario, int idResposta, boolean positiva) throws Exception {
        if (idUsuario <= 0 || idResposta <= 0) {
            return false;
        }

        ChavePar chave = new ChavePar(idUsuario, idResposta);
        if (indiceAvaliacao.buscar(chave) != null) {
            return false;
        }

        Resposta resposta = arquivoResposta.read(idResposta);
        if (resposta == null || !resposta.ativa || resposta.avaliacoes == Short.MAX_VALUE) {
            return false;
        }

        int novaNota = resposta.nota + (positiva ? 1 : -1);
        if (novaNota < Short.MIN_VALUE || novaNota > Short.MAX_VALUE) {
            return false;
        }

        Avaliacao avaliacao = new Avaliacao(idUsuario, idResposta, positiva);
        int idAvaliacao = arquivoAvaliacao.create(avaliacao);
        resposta.nota = (short) novaNota;
        resposta.avaliacoes++;
        if (!arquivoResposta.update(resposta)) {
            arquivoAvaliacao.delete(idAvaliacao);
            return false;
        }
        indiceAvaliacao.inserir(chave, idAvaliacao);
        return true;
    }

    public boolean jaAvaliou(int idUsuario, int idResposta) {
        return indiceAvaliacao.buscar(new ChavePar(idUsuario, idResposta)) != null;
    }

    public void fechar() throws Exception {
        try {
            arquivoResposta.close();
        } finally {
            arquivoAvaliacao.close();
        }
    }
}