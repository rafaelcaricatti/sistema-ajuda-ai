import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Interface textual e controle de navegação do AJUDA AÍ 1.0.
 *
 * Parte do Integrante 4: menus em console, integração entre usuários/perguntas
 * e associação entre numeração sequencial da tela e IDs internos.
 */
public class InterfaceSistema {

    private final Scanner scanner;
    private final ControladorUsuario controladorUsuario;
    private final GerenciadorPerguntas gerenciadorPerguntas;
    private final DateTimeFormatter formatadorData;

    public InterfaceSistema() throws Exception {
        scanner = new Scanner(System.in);
        controladorUsuario = new ControladorUsuario();
        gerenciadorPerguntas = new GerenciadorPerguntas();
        formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault());
    }

    public void executar() throws Exception {
        boolean executando = true;

        while (executando) {
            cabecalho("Acesso");
            System.out.println("(A) Login");
            System.out.println("(B) Novo usuário");
            System.out.println("(S) Sair");

            switch (lerOpcao()) {
                case "A":
                    Usuario usuario = login();
                    if (usuario != null) {
                        menuPrincipal(usuario);
                    }
                    break;
                case "B":
                    cadastrarUsuario();
                    break;
                case "S":
                    executando = false;
                    break;
                default:
                    mensagem("Opção inválida.");
            }
        }

        System.out.println("Sistema encerrado.");
    }

    private Usuario login() throws Exception {
        while (true) {
            cabecalho("Login");
            String email = lerTextoObrigatorio("Email: ");
            String senha = lerTextoObrigatorio("Senha: ");

            Usuario usuario = controladorUsuario.login(email, senha);
            if (usuario != null) {
                mensagem("Login realizado com sucesso. Bem-vindo(a), " + usuario.nome + "!");
                return usuario;
            }

            System.out.println("\nEmail ou senha incorretos.");
            System.out.println("(A) Tentar novamente");
            System.out.println("(B) Recuperar senha");
            System.out.println("(R) Retornar");

            switch (lerOpcao()) {
                case "A":
                    break;
                case "B":
                    recuperarSenha();
                    break;
                case "R":
                    return null;
                default:
                    mensagem("Opção inválida.");
            }
        }
    }

    private void cadastrarUsuario() throws Exception {
        cabecalho("Novo usuário");

        // O enunciado pede que o email seja verificado antes dos demais dados.
        String email = lerTextoObrigatorio("Email: ");
        if (controladorUsuario.getArquivoUsuarios().existeEmail(email)) {
            mensagem("Já existe um usuário cadastrado com este email.");
            return;
        }

        String nome = lerTextoObrigatorio("Nome completo: ");
        String senha = lerTextoObrigatorio("Senha: ");
        String perguntaSecreta = lerTextoObrigatorio("Pergunta de recuperação: ");
        String respostaSecreta = lerTextoObrigatorio("Resposta secreta: ");

        controladorUsuario.cadastrar(nome, email, senha, perguntaSecreta, respostaSecreta);
        mensagem("Usuário cadastrado com sucesso. Faça o login para acessar o sistema.");
    }

    private void recuperarSenha() throws Exception {
        cabecalho("Recuperação de senha");

        String email = lerTextoObrigatorio("Email: ");
        String pergunta = controladorUsuario.consultarPerguntaSecreta(email);

        if (pergunta == null) {
            mensagem("Usuário não encontrado.");
            return;
        }

        System.out.println("Pergunta secreta: " + pergunta);
        String resposta = lerTextoObrigatorio("Resposta: ");
        String novaSenha = lerTextoObrigatorio("Nova senha: ");

        if (controladorUsuario.recuperarSenha(email, resposta, novaSenha)) {
            mensagem("Senha alterada com sucesso.");
        } else {
            mensagem("Resposta secreta incorreta.");
        }
    }

    private void menuPrincipal(Usuario usuario) throws Exception {
        boolean logado = true;

        while (logado) {
            cabecalho("Início");
            System.out.println("(A) Minha área");
            System.out.println("(B) Buscar perguntas");
            System.out.println("(S) Sair");

            switch (lerOpcao()) {
                case "A":
                    menuMinhaArea(usuario);
                    break;
                case "B":
                    mensagem("Busca de perguntas será implementada em uma etapa posterior do projeto.");
                    break;
                case "S":
                    logado = false;
                    break;
                default:
                    mensagem("Opção inválida.");
            }
        }
    }

    private void menuMinhaArea(Usuario usuario) throws Exception {
        boolean voltar = false;

        while (!voltar) {
            cabecalho("Início > Minha área");
            System.out.println("(A) Meus dados");
            System.out.println("(B) Minhas perguntas");
            System.out.println("(C) Minhas respostas");
            System.out.println("(D) Meus votos");
            System.out.println("(R) Retornar ao menu anterior");

            switch (lerOpcao()) {
                case "A":
                    menuMeusDados(usuario);
                    break;
                case "B":
                    menuMinhasPerguntas(usuario);
                    break;
                case "C":
                case "D":
                    mensagem("Funcionalidade prevista para uma etapa posterior do projeto.");
                    break;
                case "R":
                    voltar = true;
                    break;
                default:
                    mensagem("Opção inválida.");
            }
        }
    }

    private void menuMeusDados(Usuario usuario) throws Exception {
        boolean voltar = false;

        while (!voltar) {
            cabecalho("Início > Minha área > Meus dados");
            System.out.println("(A) Alterar nome");
            System.out.println("(B) Alterar email");
            System.out.println("(C) Alterar senha");
            System.out.println("(D) Alterar pergunta e resposta de recuperação da senha");
            System.out.println("(R) Retornar ao menu anterior");

            switch (lerOpcao()) {
                case "A":
                    alterarNome(usuario);
                    break;
                case "B":
                    alterarEmail(usuario);
                    break;
                case "C":
                    alterarSenha(usuario);
                    break;
                case "D":
                    alterarRecuperacao(usuario);
                    break;
                case "R":
                    voltar = true;
                    break;
                default:
                    mensagem("Opção inválida.");
            }
        }
    }

    private void alterarNome(Usuario usuario) throws Exception {
        String nomeAnterior = usuario.nome;
        String novoNome = lerTextoObrigatorio("Novo nome: ");
        usuario.nome = novoNome;

        if (controladorUsuario.getArquivoUsuarios().update(usuario)) {
            mensagem("Nome alterado com sucesso.");
        } else {
            usuario.nome = nomeAnterior;
            mensagem("Não foi possível alterar o nome.");
        }
    }

    private void alterarEmail(Usuario usuario) throws Exception {
        String emailAnterior = usuario.email;
        String novoEmail = lerTextoObrigatorio("Novo email: ");

        if (emailAnterior.equals(novoEmail)) {
            mensagem("O novo email é igual ao email atual.");
            return;
        }

        if (controladorUsuario.getArquivoUsuarios().existeEmail(novoEmail)) {
            mensagem("Já existe um usuário cadastrado com este email.");
            return;
        }

        usuario.email = novoEmail;
        try {
            if (controladorUsuario.getArquivoUsuarios().update(usuario)) {
                mensagem("Email alterado com sucesso.");
            } else {
                usuario.email = emailAnterior;
                mensagem("Não foi possível alterar o email.");
            }
        } catch (Exception e) {
            usuario.email = emailAnterior;
            throw e;
        }
    }

    private void alterarSenha(Usuario usuario) throws Exception {
        String hashAnterior = usuario.hashSenha;
        String novaSenha = lerTextoObrigatorio("Nova senha: ");
        usuario.hashSenha = Seguranca.gerarHash(novaSenha);

        if (controladorUsuario.getArquivoUsuarios().update(usuario)) {
            mensagem("Senha alterada com sucesso.");
        } else {
            usuario.hashSenha = hashAnterior;
            mensagem("Não foi possível alterar a senha.");
        }
    }

    private void alterarRecuperacao(Usuario usuario) throws Exception {
        String perguntaAnterior = usuario.perguntaSecreta;
        String hashRespostaAnterior = usuario.hashRespostaSecreta;

        String novaPergunta = lerTextoObrigatorio("Nova pergunta de recuperação: ");
        String novaResposta = lerTextoObrigatorio("Nova resposta secreta: ");

        usuario.perguntaSecreta = novaPergunta;
        usuario.hashRespostaSecreta = Seguranca.gerarHashRespostaSecreta(novaResposta);

        if (controladorUsuario.getArquivoUsuarios().update(usuario)) {
            mensagem("Pergunta e resposta de recuperação alteradas com sucesso.");
        } else {
            usuario.perguntaSecreta = perguntaAnterior;
            usuario.hashRespostaSecreta = hashRespostaAnterior;
            mensagem("Não foi possível alterar os dados de recuperação.");
        }
    }

    private void menuMinhasPerguntas(Usuario usuario) throws Exception {
        boolean voltar = false;

        while (!voltar) {
            cabecalho("Início > Minha área > Minhas perguntas");
            System.out.println("(A) Listar");
            System.out.println("(B) Incluir");
            System.out.println("(C) Alterar");
            System.out.println("(D) Arquivar");
            System.out.println("(R) Retornar ao menu anterior");

            switch (lerOpcao()) {
                case "A":
                    listarPerguntas(usuario);
                    break;
                case "B":
                    incluirPergunta(usuario);
                    break;
                case "C":
                    alterarPergunta(usuario);
                    break;
                case "D":
                    arquivarPergunta(usuario);
                    break;
                case "R":
                    voltar = true;
                    break;
                default:
                    mensagem("Opção inválida.");
            }
        }
    }

    private void listarPerguntas(Usuario usuario) throws Exception {
        cabecalho("MINHAS PERGUNTAS");
        List<Pergunta> perguntas = gerenciadorPerguntas.listarPerguntasDoUsuario(usuario.idUsuario);
        imprimirPerguntas(perguntas);
        pausar();
    }

    private void incluirPergunta(Usuario usuario) throws Exception {
        cabecalho("Incluir pergunta");

        String texto = lerTextoObrigatorio("Pergunta: ");
        String palavrasChave = lerTextoObrigatorio("Palavras chave (separadas por ;): ");

        gerenciadorPerguntas.incluirPergunta(usuario.idUsuario, texto, palavrasChave);
        mensagem("Pergunta incluída com sucesso.");
    }

    private void alterarPergunta(Usuario usuario) throws Exception {
        cabecalho("Alterar pergunta");
        List<Pergunta> perguntas = gerenciadorPerguntas.listarPerguntasDoUsuario(usuario.idUsuario);

        if (perguntas.isEmpty()) {
            mensagem("Você ainda não possui perguntas cadastradas.");
            return;
        }

        imprimirPerguntas(perguntas);
        int numero = lerNumeroSequencial("Número da pergunta que deseja alterar: ", perguntas.size());
        if (numero == -1) {
            mensagem("Número inválido.");
            return;
        }

        Pergunta selecionada = perguntas.get(numero - 1);
        if (!selecionada.ativa) {
            mensagem("Perguntas arquivadas não podem ser alteradas.");
            return;
        }

        System.out.println("\nPergunta atual: " + selecionada.pergunta);
        System.out.println("Palavras chave atuais: " + selecionada.palavrasChave);

        String novoTexto = lerTextoObrigatorio("Nova pergunta: ");
        String novasPalavrasChave = lerTextoObrigatorio("Novas palavras chave: ");

        if (gerenciadorPerguntas.alterarPergunta(
                usuario.idUsuario,
                selecionada.idPergunta,
                novoTexto,
                novasPalavrasChave)) {
            mensagem("Pergunta alterada com sucesso.");
        } else {
            mensagem("Não foi possível alterar a pergunta.");
        }
    }

    private void arquivarPergunta(Usuario usuario) throws Exception {
        cabecalho("Arquivar pergunta");
        List<Pergunta> perguntas = gerenciadorPerguntas.listarPerguntasDoUsuario(usuario.idUsuario);

        if (perguntas.isEmpty()) {
            mensagem("Você ainda não possui perguntas cadastradas.");
            return;
        }

        imprimirPerguntas(perguntas);
        int numero = lerNumeroSequencial("Número da pergunta que deseja arquivar: ", perguntas.size());
        if (numero == -1) {
            mensagem("Número inválido.");
            return;
        }

        Pergunta selecionada = perguntas.get(numero - 1);
        if (!selecionada.ativa) {
            mensagem("Essa pergunta já está arquivada.");
            return;
        }

        String confirmacao = lerTexto("Confirmar arquivamento definitivo? (S/N): ").toUpperCase();
        if (!"S".equals(confirmacao)) {
            mensagem("Arquivamento cancelado.");
            return;
        }

        if (gerenciadorPerguntas.arquivarPergunta(usuario.idUsuario, selecionada.idPergunta)) {
            mensagem("Pergunta arquivada com sucesso.");
        } else {
            mensagem("Não foi possível arquivar a pergunta.");
        }
    }

    /**
     * Imprime as perguntas com números sequenciais (1, 2, 3...).
     * Os IDs internos do usuário e da pergunta nunca são exibidos.
     */
    private void imprimirPerguntas(List<Pergunta> perguntas) {
        if (perguntas.isEmpty()) {
            System.out.println("Nenhuma pergunta cadastrada.");
            return;
        }

        for (int i = 0; i < perguntas.size(); i++) {
            Pergunta pergunta = perguntas.get(i);
            String estado = pergunta.ativa ? "" : " ARQUIVADA";

            System.out.println("(" + (i + 1) + ")" + estado);
            System.out.println(formatadorData.format(Instant.ofEpochMilli(pergunta.criacao)));
            System.out.println(pergunta.pergunta);
            System.out.println("Palavras chave: " + pergunta.palavrasChave);
            System.out.println();
        }
    }

    private int lerNumeroSequencial(String mensagem, int quantidade) {
        String valor = lerTexto(mensagem);
        try {
            int numero = Integer.parseInt(valor);
            if (numero >= 1 && numero <= quantidade) {
                return numero;
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }

    private void cabecalho(String caminho) {
        System.out.println("\nAJUDA AÍ 1.0");
        System.out.println("------------");
        if (caminho != null && !caminho.isEmpty() && !"Acesso".equals(caminho)) {
            System.out.println("> " + caminho);
        }
    }

    private String lerOpcao() {
        return lerTexto("Opção: ").toUpperCase();
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private String lerTextoObrigatorio(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("O valor não pode ficar vazio.");
        }
    }

    private void mensagem(String texto) {
        System.out.println("\n" + texto);
        pausar();
    }

    private void pausar() {
        System.out.println("Pressione ENTER para continuar...");
        scanner.nextLine();
    }

    public void fechar() throws Exception {
        Exception erro = null;

        try {
            gerenciadorPerguntas.fechar();
        } catch (Exception e) {
            erro = e;
        }

        try {
            controladorUsuario.getArquivoUsuarios().close();
        } catch (Exception e) {
            if (erro == null) {
                erro = e;
            }
        }

        scanner.close();

        if (erro != null) {
            throw erro;
        }
    }
}
