import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


 // Regras de negócio das interações: responder, votar e arquivar a própria resposta.
 // A interface chama estes métodos; aqui ficam as validações.

public class GerenciadorRespostas {

    // O CRUD genérico guarda o tamanho do registro em um short (máx. 32767 bytes).
    private static final int MAX_BYTES_RESPOSTA = 30000;

    private final ArquivoResposta arquivoResposta;
    private final ArquivoAvaliacao arquivoAvaliacao;

    public GerenciadorRespostas() throws Exception {
        this.arquivoResposta = new ArquivoResposta();
        this.arquivoAvaliacao = new ArquivoAvaliacao();
    }

    // 1) RESPONDER: devolve o id da resposta criada ou -1 se o texto for inválido. 
    public int responder(int idUsuario, int idPergunta, String texto) throws Exception {
        if (texto == null || texto.trim().isEmpty()) {
            return -1;
        }
        if (texto.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_RESPOSTA) {
            return -1;
        }
        // nota=0, avaliacoes=0, ativa=true e criacao=agora são preenchidos pelo construtor
        Resposta resposta = new Resposta(idPergunta, idUsuario, texto.trim());
        return arquivoResposta.create(resposta); // grava no arquivo + atualiza as duas Árvores B+
    }

    // Resultados possíveis de um voto, para a interface mostrar a mensagem certa. 
    public enum ResultadoVoto { OK, RESPOSTA_NAO_ENCONTRADA, RESPOSTA_ARQUIVADA, PROPRIA_RESPOSTA, JA_VOTOU }

    // 2) VOTAR: concordar (true) ou discordar (false). 
    public ResultadoVoto votar(int idUsuario, int idResposta, boolean concordou) throws Exception {
        Resposta resposta = arquivoResposta.read(idResposta);
        if (resposta == null) {
            return ResultadoVoto.RESPOSTA_NAO_ENCONTRADA;
        }
        if (!resposta.ativa) {
            return ResultadoVoto.RESPOSTA_ARQUIVADA;
        }
        if (resposta.idUsuario == idUsuario) {        // não pode votar na própria resposta
            return ResultadoVoto.PROPRIA_RESPOSTA;
        }
        if (arquivoAvaliacao.buscar(idUsuario, idResposta) != null) { // trava anti-voto duplo
            return ResultadoVoto.JA_VOTOU;
        }

        // 1º grava a avaliação (é ela que impede o voto duplo)
        arquivoAvaliacao.create(new Avaliacao(idUsuario, idResposta, concordou));

        // 2º ajusta a resposta: nota +1/-1 e avaliacoes +1
        resposta.nota += concordou ? 1 : -1;
        resposta.avaliacoes += 1;
        arquivoResposta.update(resposta);
        return ResultadoVoto.OK;
    }

    /** 3) ARQUIVAR a própria resposta (exclusão lógica: ativa = false). */
    public boolean arquivarResposta(int idUsuario, int idResposta) throws Exception {
        Resposta resposta = arquivoResposta.read(idResposta);
        if (resposta == null || resposta.idUsuario != idUsuario || !resposta.ativa) {
            return false;
        }
        resposta.arquivar();
        return arquivoResposta.update(resposta);
    }

    // onsultas de apoio (úteis para a tela e para os testes) 

    // Respostas ativas de uma pergunta, da maior nota para a menor. 
    public List<Resposta> listarRespostasDaPergunta(int idPergunta) throws Exception {
        List<Resposta> ativas = new ArrayList<>();
        for (Resposta r : Arrays.asList(arquivoResposta.readAllByPergunta(idPergunta))) {
            if (r.ativa) {
                ativas.add(r);
            }
        }
        ativas.sort(Comparator.comparingInt((Resposta r) -> r.nota).reversed());
        return ativas;
    }

    public Resposta buscarResposta(int idResposta) throws Exception {
        return arquivoResposta.read(idResposta);
    }

    public void fechar() throws Exception {
        arquivoResposta.close();
        arquivoAvaliacao.close();
    }
}
