import java.io.File;
import java.util.List;

public class TestePerguntas {
    public static void limparArquivosPersistidos() {
        File dados = new File("./dados");
        if (dados.exists() && dados.isDirectory()) {
            deletarRecursivo(dados);
        }
    }

    private static void deletarRecursivo(File arquivo) {
        if (arquivo.isDirectory()) {
            File[] filhos = arquivo.listFiles();
            if (filhos != null) {
                for (File filho : filhos) {
                    deletarRecursivo(filho);
                }
            }
        }
        arquivo.delete();
    }

    public static void main(String[] args) throws Exception {
        limparArquivosPersistidos();

        GerenciadorPerguntas gp = new GerenciadorPerguntas();

        int idUsuario = 1;
        int id1 = gp.incluirPergunta(idUsuario, "É seguro comer pão mofado?", "pão;mofado;saúde");
        int id2 = gp.incluirPergunta(idUsuario, "Qual linguagem começar a programar?", "programação;linguagem");

        if (id1 <= 0 || id2 <= 0) {
            throw new IllegalStateException("Perguntas não foram criadas corretamente");
        }

        List<Pergunta> perguntas = gp.listarPerguntasDoUsuario(idUsuario);
        if (perguntas.size() != 2) {
            throw new IllegalStateException("Esperava 2 perguntas para o usuário. Encontrou: " + perguntas.size());
        }

        boolean alterou = gp.alterarPergunta(id1, "É seguro comer pão mofado depois de cortar a parte estragada?", "pão;mofo;saúde;alimentação");
        if (!alterou) {
            throw new IllegalStateException("Não foi possível alterar a pergunta");
        }

        boolean arquivou = gp.arquivarPergunta(id2);
        if (!arquivou) {
            throw new IllegalStateException("Não foi possível arquivar a pergunta");
        }

        List<Pergunta> ativas = gp.listarPerguntasAtivasDoUsuario(idUsuario);
        if (ativas.size() != 1) {
            throw new IllegalStateException("Esperava 1 pergunta ativa. Encontrou: " + ativas.size());
        }

        System.out.println("Teste de perguntas OK");
    }
}
