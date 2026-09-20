import java.util.List;

public class TesteIntegracao {

    public static void main(String[] args) throws Exception {
        ControladorUsuario controlador = new ControladorUsuario();
        Usuario usuario = controlador.cadastrar(
                "Teste Integração",
                "teste.integracao@puc.br",
                "senha123",
                "Em qual cidade você nasceu?",
                "São Paulo"
        );

        if (controlador.login("teste.integracao@puc.br", "senha-errada") != null) {
            throw new IllegalStateException("Login incorreto foi aceito.");
        }

        if (controlador.login("teste.integracao@puc.br", "senha123") == null) {
            throw new IllegalStateException("Login correto falhou.");
        }

        if (!controlador.recuperarSenha(
                "teste.integracao@puc.br",
                "sao paulo",
                "novaSenha456")) {
            throw new IllegalStateException("Normalização da resposta secreta falhou.");
        }

        if (controlador.login("teste.integracao@puc.br", "novaSenha456") == null) {
            throw new IllegalStateException("Nova senha não foi aceita.");
        }

        GerenciadorPerguntas gp = new GerenciadorPerguntas();
        int id1 = gp.incluirPergunta(usuario.idUsuario,
                "Primeira pergunta de teste?",
                "teste;primeira");
        int id2 = gp.incluirPergunta(usuario.idUsuario,
                "Segunda pergunta de teste?",
                "teste;segunda");

        List<Pergunta> perguntas = gp.listarPerguntasDoUsuario(usuario.idUsuario);
        if (perguntas.size() != 2) {
            throw new IllegalStateException("A Árvore B+ não retornou as duas perguntas.");
        }

        if (!gp.alterarPergunta(usuario.idUsuario, id1,
                "Primeira pergunta alterada?",
                "teste;alterada")) {
            throw new IllegalStateException("Alteração da pergunta falhou.");
        }

        if (!gp.arquivarPergunta(usuario.idUsuario, id2)) {
            throw new IllegalStateException("Arquivamento da pergunta falhou.");
        }

        gp.fechar();
        controlador.getArquivoUsuarios().close();

        // Reabre o gerenciador para confirmar que o relacionamento está persistido
        // e que a pergunta arquivada continua visível para o autor.
        gp = new GerenciadorPerguntas();
        perguntas = gp.listarPerguntasDoUsuario(usuario.idUsuario);
        if (perguntas.size() != 2) {
            throw new IllegalStateException("Relacionamento não persistiu após reabrir o sistema.");
        }

        long arquivadas = perguntas.stream().filter(p -> !p.ativa).count();
        if (arquivadas != 1) {
            throw new IllegalStateException("Pergunta arquivada não permaneceu visível ao autor.");
        }

        gp.fechar();
        System.out.println("TESTE DE INTEGRAÇÃO OK");
    }
}
