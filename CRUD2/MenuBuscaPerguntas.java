import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MenuBuscaPerguntas {

    private static final int TAMANHO_PAGINA = 5;

    private final Scanner scanner;
    private final GerenciadorPerguntas gerenciadorPerguntas;
    private final GerenciadorRespostas gerenciadorRespostas;
    private final ArquivoUsuario arquivoUsuarios;
    private final MotorBusca motorBusca;
    private final DateTimeFormatter formatadorData;

    // O GerenciadorRespostas é compartilhado com a Minha área para que os arquivos
    // de respostas/avaliações fiquem abertos uma única vez.
    public MenuBuscaPerguntas(Scanner scanner,
                              GerenciadorPerguntas gerenciadorPerguntas,
                              GerenciadorRespostas gerenciadorRespostas,
                              ArquivoUsuario arquivoUsuarios) {
        this(scanner, gerenciadorPerguntas, gerenciadorRespostas, arquivoUsuarios, null);
    }

    public MenuBuscaPerguntas(Scanner scanner,
                              GerenciadorPerguntas gerenciadorPerguntas,
                              GerenciadorRespostas gerenciadorRespostas,
                              ArquivoUsuario arquivoUsuarios,
                              MotorBusca motorBusca) {
        this.scanner = scanner;
        this.gerenciadorPerguntas = gerenciadorPerguntas;
        this.gerenciadorRespostas = gerenciadorRespostas;
        this.arquivoUsuarios = arquivoUsuarios;
        this.motorBusca = motorBusca;
        this.formatadorData = DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'às' HH:mm")
                .withZone(ZoneId.systemDefault());
    }

    public void exibir(Usuario usuarioLogado) throws Exception {
        boolean voltar = false;

        while (!voltar) {
            cabecalho("Início > Buscar perguntas");
            System.out.println("(A) Perguntas mais recentes");
            System.out.println("(B) Busca por palavras-chave");
            System.out.println();
            System.out.println("(R) Retornar");

            switch (lerOpcao()) {
                case "A":
                    exibirPerguntasRecentes(usuarioLogado, gerenciadorRespostas);
                    break;
                case "B":
                    if (motorBusca == null) {
                        mensagem("Busca por palavras-chave indisponível: índice não configurado.");
                    } else {
                        exibirBuscaPorPalavrasChave(usuarioLogado);
                    }
                    break;
                case "R":
                    voltar = true;
                    break;
                default:
                    mensagem("Opção inválida.");
            }
        }
    }

    private void exibirBuscaPorPalavrasChave(Usuario usuarioLogado) throws Exception {
        String caminhoBusca = "Início > Buscar perguntas > Busca por palavras-chave";
        cabecalho(caminhoBusca);

        String consulta = lerTextoObrigatorio("Termos de busca: ");
        List<ResultadoBusca> ranking = motorBusca.buscar(consulta);
        List<ResultadoBusca> resultadosAtivos = new ArrayList<>();
        List<Pergunta> perguntasAtivas = new ArrayList<>();

        for (ResultadoBusca resultado : ranking) {
            Pergunta pergunta = gerenciadorPerguntas.buscarPergunta(resultado.getIdPergunta());
            if (pergunta != null && pergunta.ativa) {
                resultadosAtivos.add(resultado);
                perguntasAtivas.add(pergunta);
            }
        }

        if (resultadosAtivos.isEmpty()) {
            mensagem("Nenhuma pergunta ativa encontrada para esses termos.");
            return;
        }

        while (true) {
            cabecalho(caminhoBusca);

            for (int i = 0; i < resultadosAtivos.size(); i++) {
                Pergunta pergunta = perguntasAtivas.get(i);
                Usuario autor = arquivoUsuarios.read(pergunta.idUsuario);
                String nomeAutor = autor == null ? "Usuário não encontrado" : autor.nome;

                System.out.printf("(%d) %s%n", i + 1, pergunta.pergunta);
                System.out.printf("    Relevância: %.4f%n", resultadosAtivos.get(i).getPeso());
                System.out.println("    Criada por " + nomeAutor + " em "
                        + formatadorData.format(Instant.ofEpochMilli(pergunta.criacao)));
                System.out.println();
            }

            System.out.println("(R) Retornar");
            String opcao = lerOpcao();
            if ("R".equals(opcao)) {
                return;
            }

            int numero = converterNumero(opcao);
            if (numero >= 1 && numero <= perguntasAtivas.size()) {
                exibirPergunta(
                        usuarioLogado,
                        perguntasAtivas.get(numero - 1),
                        gerenciadorRespostas,
                        caminhoBusca + " > Pergunta"
                );
            } else {
                mensagem("Opção inválida.");
            }
        }
    }

    private void exibirPerguntasRecentes(Usuario usuarioLogado,
                                         GerenciadorRespostas gerenciadorRespostas) throws Exception {
        List<Pergunta> perguntas = gerenciadorPerguntas.listarPerguntasMaisRecentes();

        if (perguntas.isEmpty()) {
            mensagem("Nenhuma pergunta ativa encontrada.");
            return;
        }

        int pagina = 0;

        while (true) {
            int totalPaginas = (perguntas.size() + TAMANHO_PAGINA - 1) / TAMANHO_PAGINA;

            if (pagina >= totalPaginas) {
                pagina = totalPaginas - 1;
            }

            int inicio = pagina * TAMANHO_PAGINA;
            int fim = Math.min(inicio + TAMANHO_PAGINA, perguntas.size());

            cabecalho("Início > Buscar perguntas > Perguntas mais recentes");

            for (int i = inicio; i < fim; i++) {
                Pergunta pergunta = perguntas.get(i);
                Usuario autor = arquivoUsuarios.read(pergunta.idUsuario);

                String nomeAutor = autor == null
                        ? "Usuário não encontrado"
                        : autor.nome;

                int numeroTela = i - inicio + 1;

                System.out.println("(" + numeroTela + ") " + pergunta.pergunta);
                System.out.println("    Criada por " + nomeAutor
                        + " em " + formatadorData.format(
                                Instant.ofEpochMilli(pergunta.criacao)
                        ));
                System.out.println();
            }

            System.out.println("Página " + (pagina + 1) + " de " + totalPaginas);
            System.out.println();
            System.out.println("(A) Página anterior");
            System.out.println("(B) Próxima página");
            System.out.println();
            System.out.println("(R) Retornar");

            String opcao = lerOpcao();

            if ("R".equals(opcao)) {
                return;
            }

            if ("A".equals(opcao)) {
                if (pagina > 0) {
                    pagina--;
                } else {
                    mensagem("Você já está na primeira página.");
                }
                continue;
            }

            if ("B".equals(opcao)) {
                if (pagina + 1 < totalPaginas) {
                    pagina++;
                } else {
                    mensagem("Não há uma próxima página.");
                }
                continue;
            }

            int numero = converterNumero(opcao);

            if (numero >= 1 && numero <= (fim - inicio)) {
                Pergunta selecionada = perguntas.get(inicio + numero - 1);
                exibirPergunta(
                        usuarioLogado,
                        selecionada,
                        gerenciadorRespostas,
                        "Início > Buscar perguntas > Perguntas mais recentes > Pergunta"
                );
            } else {
                mensagem("Opção inválida.");
            }
        }
    }

    private void exibirPergunta(Usuario usuarioLogado,
                                Pergunta pergunta,
                                GerenciadorRespostas gerenciadorRespostas,
                                String caminhoPergunta) throws Exception {
        boolean voltar = false;

        while (!voltar) {
            Usuario autorPergunta = arquivoUsuarios.read(pergunta.idUsuario);
            String nomeAutorPergunta = autorPergunta == null
                    ? "Usuário não encontrado"
                    : autorPergunta.nome;

            List<Resposta> respostas =
                    gerenciadorRespostas.listarRespostasDaPergunta(
                            pergunta.idPergunta
                    );

            cabecalho(caminhoPergunta);

            System.out.println(pergunta.pergunta);
            System.out.println(
                    "Criada por " + nomeAutorPergunta
                            + " em " + formatadorData.format(
                                    Instant.ofEpochMilli(pergunta.criacao)
                            )
            );

            System.out.println();
            System.out.println("RESPOSTAS");
            System.out.println("---------");

            if (respostas.isEmpty()) {
                System.out.println("Nenhuma resposta cadastrada.");
            } else {
                for (int i = 0; i < respostas.size(); i++) {
                    Resposta resposta = respostas.get(i);
                    Usuario autorResposta = arquivoUsuarios.read(
                            resposta.idUsuario
                    );

                    String nomeAutorResposta = autorResposta == null
                            ? "Usuário não encontrado"
                            : autorResposta.nome;

                    System.out.println();
                    System.out.println("(" + (i + 1) + ") " + resposta.resposta);
                    System.out.println(
                            "Respondido por " + nomeAutorResposta
                                    + " em " + formatadorData.format(
                                            Instant.ofEpochMilli(
                                                    resposta.criacao
                                            )
                                    )
                    );
                    System.out.println(
                            "Nota: " + resposta.nota
                                    + " (" + resposta.avaliacoes
                                    + " avaliações)"
                    );
                }
            }

            System.out.println();
            System.out.println("(A) Responder à pergunta");
            System.out.println();
            System.out.println("(R) Retornar");

            String opcao = lerOpcao();

            if ("R".equals(opcao)) {
                voltar = true;
                continue;
            }

            if ("A".equals(opcao)) {
                responderPergunta(
                        usuarioLogado,
                        pergunta,
                        nomeAutorPergunta,
                        gerenciadorRespostas,
                        caminhoPergunta + " > Responder"
                );
                continue;
            }

            int numero = converterNumero(opcao);

            if (numero >= 1 && numero <= respostas.size()) {
                Resposta selecionada = respostas.get(numero - 1);
                Usuario autorResposta = arquivoUsuarios.read(
                        selecionada.idUsuario
                );

                String nomeAutorResposta = autorResposta == null
                        ? "Usuário não encontrado"
                        : autorResposta.nome;

                MenuResposta menuResposta = new MenuResposta(
                        scanner,
                        gerenciadorRespostas
                );

                menuResposta.exibir(
                        usuarioLogado,
                        pergunta,
                        nomeAutorPergunta,
                        selecionada,
                        nomeAutorResposta,
                        caminhoPergunta,
                        false
                );
            } else {
                mensagem("Opção inválida.");
            }
        }
    }

    private void responderPergunta(Usuario usuarioLogado,
                                   Pergunta pergunta,
                                   String nomeAutorPergunta,
                                   GerenciadorRespostas gerenciadorRespostas,
                                   String caminhoResposta) throws Exception {
        cabecalho(caminhoResposta);

        System.out.println(pergunta.pergunta);
        System.out.println(
                "Criada por " + nomeAutorPergunta
                        + " em " + formatadorData.format(
                                Instant.ofEpochMilli(pergunta.criacao)
                        )
        );
        System.out.println();

        String texto = lerTextoObrigatorio("Resposta: ");

        int idResposta = gerenciadorRespostas.responder(
                usuarioLogado.idUsuario,
                pergunta.idPergunta,
                texto
        );

        if (idResposta > 0) {
            mensagem("Resposta cadastrada com sucesso.");
        } else {
            mensagem("Não foi possível cadastrar a resposta.");
        }
    }

    private int converterNumero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void cabecalho(String caminho) {
        System.out.println();
        System.out.println("AJUDA AÍ 1.0");
        System.out.println("------------");
        System.out.println("> " + caminho);
        System.out.println();
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
        System.out.println();
        System.out.println(texto);
        System.out.println("Pressione ENTER para continuar...");
        scanner.nextLine();
    }
}
