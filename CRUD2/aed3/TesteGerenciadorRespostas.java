import java.io.File;
import java.util.List;

public class TesteGerenciadorRespostas {

    public static void main(String[] args) throws Exception {
        String sufixo = "teste_respostas_" + Long.toHexString(System.nanoTime());
        String nomeRespostas = sufixo + "_dados";
        String nomeAvaliacoes = sufixo + "_votos";
        GerenciadorRespostas gerenciador = null;

        try {
            gerenciador = new GerenciadorRespostas(nomeRespostas, nomeAvaliacoes);
            int idResposta1 = gerenciador.incluirResposta(10, 20, "Primeira resposta");
            int idResposta2 = gerenciador.incluirResposta(10, 21, "Segunda resposta");

            verificar(gerenciador.listarRespostasDaPergunta(10).size() == 2,
                    "Índice por pergunta não encontrou as respostas");
            verificar(gerenciador.listarRespostasDoUsuario(20).size() == 1,
                    "Índice por usuário não encontrou a resposta");
            verificar(gerenciador.avaliarResposta(30, idResposta1, true),
                    "Não foi possível registrar avaliação positiva");
            verificar(!gerenciador.avaliarResposta(30, idResposta1, false),
                    "O sistema aceitou voto duplicado");
            verificar(gerenciador.avaliarResposta(31, idResposta1, false),
                    "Não foi possível registrar avaliação negativa");

            Resposta resposta = gerenciador.buscarResposta(idResposta1);
            verificar(resposta.nota == 0 && resposta.avaliacoes == 2,
                    "Nota e quantidade de avaliações ficaram incorretas");
            verificar(gerenciador.arquivarResposta(idResposta2), "Não foi possível arquivar a resposta");
            verificar(gerenciador.listarRespostasDaPergunta(10).size() == 1,
                    "A listagem não filtrou resposta arquivada");

            gerenciador.fechar();
            gerenciador = null;

            gerenciador = new GerenciadorRespostas(nomeRespostas, nomeAvaliacoes);
            resposta = gerenciador.buscarResposta(idResposta1);
            verificar(resposta.nota == 0 && resposta.avaliacoes == 2,
                    "Os totais de avaliação não foram reconstruídos após reinício");
            verificar(gerenciador.jaAvaliou(30, idResposta1),
                    "O índice de avaliações não foi reconstruído após reinício");
            verificar(!gerenciador.avaliarResposta(30, idResposta1, true),
                    "O sistema aceitou voto duplicado após reinício");
            verificar(gerenciador.listarRespostasDaPergunta(10).size() == 1,
                    "O índice por pergunta não foi reconstruído após reinício");

            testarListagemComLacunasEAtualizacaoCrescente();
            System.out.println("Teste do gerenciador de respostas OK");
        } finally {
            if (gerenciador != null) {
                gerenciador.fechar();
            }
            apagarRecursivo(new File("dados", nomeRespostas));
            apagarRecursivo(new File("dados", nomeAvaliacoes));
        }
    }

    private static void testarListagemComLacunasEAtualizacaoCrescente() throws Exception {
        String nomeArquivo = "teste_arquivo_respostas_" + Long.toHexString(System.nanoTime());
        ArquivoResposta arquivo = null;
        try {
            arquivo = new ArquivoResposta(nomeArquivo);
            int idPequeno1 = arquivo.create(new Resposta(1, 1, "a"));
            int idGrande = arquivo.create(new Resposta(1, 1, repetir('b', 1200)));
            int idPequeno2 = arquivo.create(new Resposta(1, 1, "c"));
            verificar(arquivo.delete(idPequeno1) && arquivo.delete(idGrande),
                    "Não foi possível preparar espaços excluídos");

            Resposta resposta = arquivo.read(idPequeno2);
            resposta.resposta = repetir('d', 600);
            verificar(arquivo.update(resposta), "A atualização de registro crescente falhou");
            verificar(arquivo.read(idPequeno2).resposta.equals(resposta.resposta),
                    "A atualização crescente corrompeu o registro");

            List<Resposta> registros = arquivo.listarTodas();
            verificar(registros.size() == 1 && registros.get(0).idResposta == idPequeno2,
                    "A listagem parou antes de alcançar IDs posteriores a exclusões");
        } finally {
            if (arquivo != null) {
                arquivo.close();
            }
            apagarRecursivo(new File("dados", nomeArquivo));
        }
    }

    private static String repetir(char caractere, int quantidade) {
        StringBuilder texto = new StringBuilder(quantidade);
        for (int i = 0; i < quantidade; i++) {
            texto.append(caractere);
        }
        return texto.toString();
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static void apagarRecursivo(File arquivo) {
        if (arquivo.isDirectory()) {
            File[] filhos = arquivo.listFiles();
            if (filhos != null) {
                for (File filho : filhos) {
                    apagarRecursivo(filho);
                }
            }
        }
        arquivo.delete();
    }
}