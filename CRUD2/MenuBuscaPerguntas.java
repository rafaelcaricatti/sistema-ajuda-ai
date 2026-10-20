import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class MenuBuscaPerguntas {

    private static final int TAMANHO_PAGINA = 5;

    private final Scanner scanner;
    private final GerenciadorPerguntas gerenciadorPerguntas;
    private final GerenciadorRespostas gerenciadorRespostas;
    private final ArquivoUsuario arquivoUsuarios;
    private final DateTimeFormatter formatadorData;

    // O GerenciadorRespostas é compartilhado com a Minha área para que os arquivos
    // de respostas/avaliações fiquem abertos uma única vez.
    public MenuBuscaPerguntas(Scanner scanner,
                              GerenciadorPerguntas gerenciadorPerguntas,
                              GerenciadorRespostas gerenciadorRespostas,
                              ArquivoUsuario arquivoUsuarios) {
        this.scanner = scanner;
        this.gerenciadorPerguntas = gerenciadorPerguntas;
        this.gerenciadorRespostas = gerenciadorRespostas;
        this.arquivoUsuarios = arquivoUsuarios;
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
                    mensagem("Busca por palavras-chave será implementada em uma etapa posterior.");
                    break;
                case "R":
                    voltar = true;
                    break;
                default:
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
                        gerenciadorRespostas
                );
            } else {
                mensagem("Opção inválida.");
            }
        }
    }

    private void exibirPergunta(Usuario usuarioLogado,
                                Pergunta pergunta,
                                GerenciadorRespostas gerenciadorRespostas) throws Exception {
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

            cabecalho(
                    "Início > Buscar perguntas > Perguntas mais recentes > Pergunta"
            );

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
                        gerenciadorRespostas
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
                        "Início > Buscar perguntas > Perguntas mais recentes > Pergunta",
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
                                   GerenciadorRespostas gerenciadorRespostas) throws Exception {
        cabecalho(
                "Início > Buscar perguntas > Perguntas mais recentes > Pergunta > Responder"
        );

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
