import java.util.List;

public class TesteInteracoes {

    private static void confere(boolean cond, String msg) {
        if (!cond) throw new IllegalStateException("FALHOU: " + msg);
        System.out.println("OK  - " + msg);
    }

    public static void main(String[] args) throws Exception {
        TestePerguntas.limparArquivosPersistidos();
        GerenciadorRespostas g = new GerenciadorRespostas();

        int autor = 1, ana = 2, beto = 3, idPergunta = 10;

        // responder
        confere(g.responder(ana, idPergunta, "   ") == -1, "texto vazio é recusado");
        int r1 = g.responder(ana, idPergunta, "Porque o corpo gasta energia na digestão");
        int r2 = g.responder(beto, idPergunta, "Relógio biológico");
        confere(r1 > 0 && r2 > r1, "ids sequenciais criados");
        Resposta lida = g.buscarResposta(r1);
        confere(lida.nota == 0 && lida.avaliacoes == 0 && lida.ativa && lida.idUsuario == ana && lida.idPergunta == idPergunta,
                "campos automáticos (nota=0, avaliacoes=0, ativa=true, ids)");
        confere(g.responder(autor, 99, "outra pergunta") > 0, "resposta de outra pergunta");
        confere(g.listarRespostasDaPergunta(idPergunta).size() == 2, "árvore pergunta->resposta lista só as 2 da pergunta");

        // votar
        confere(g.votar(ana, r1, true) == GerenciadorRespostas.ResultadoVoto.PROPRIA_RESPOSTA, "autor não vota na própria");
        confere(g.votar(autor, r1, true) == GerenciadorRespostas.ResultadoVoto.OK, "voto positivo");
        confere(g.votar(autor, r1, false) == GerenciadorRespostas.ResultadoVoto.JA_VOTOU, "voto duplo bloqueado");
        confere(g.votar(beto, r1, true) == GerenciadorRespostas.ResultadoVoto.OK, "segundo usuário vota");
        confere(g.votar(autor, r2, false) == GerenciadorRespostas.ResultadoVoto.OK, "voto negativo");
        confere(g.votar(autor, 12345, true) == GerenciadorRespostas.ResultadoVoto.RESPOSTA_NAO_ENCONTRADA, "resposta inexistente");
        lida = g.buscarResposta(r1);
        confere(lida.nota == 2 && lida.avaliacoes == 2, "r1: nota 2, 2 avaliações (persistido)");
        lida = g.buscarResposta(r2);
        confere(lida.nota == -1 && lida.avaliacoes == 1, "r2: nota -1, 1 avaliação");

        // ordenação por nota
        List<Resposta> ord = g.listarRespostasDaPergunta(idPergunta);
        confere(ord.get(0).idResposta == r1, "ordenado por nota decrescente");

        // arquivar
        confere(!g.arquivarResposta(beto, r1), "não arquiva resposta de outro");
        confere(g.arquivarResposta(ana, r1), "autor arquiva a própria");
        confere(!g.arquivarResposta(ana, r1), "arquivar duas vezes falha");
        confere(g.votar(beto, r1, true) == GerenciadorRespostas.ResultadoVoto.RESPOSTA_ARQUIVADA, "não vota em arquivada");
        confere(g.listarRespostasDaPergunta(idPergunta).size() == 1, "arquivada some da listagem");
        confere(!g.buscarResposta(r1).ativa, "registro continua no arquivo com ativa=false");

        // persistência: fecha e reabre
        g.fechar();
        GerenciadorRespostas g2 = new GerenciadorRespostas();
        confere(g2.votar(autor, r2, true) == GerenciadorRespostas.ResultadoVoto.JA_VOTOU, "trava de voto duplo persiste após reabrir");
        confere(g2.buscarResposta(r2).nota == -1, "nota persiste após reabrir");
        g2.fechar();

        System.out.println("\nTODOS OS TESTES PASSARAM");
    }
}
