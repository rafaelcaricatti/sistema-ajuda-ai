import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;


// Telas da Minha área ligadas ao TP2: "Minhas respostas" e "Minhas avaliações".
// A listagem usa as Árvores B+ [idUsuario, idResposta] e [idUsuario, idAvaliacao].

public class MenuMinhasInteracoes {

    private final Scanner scanner;
    private final GerenciadorPerguntas gerenciadorPerguntas;
    private final GerenciadorRespostas gerenciadorRespostas;
    private final ArquivoUsuario arquivoUsuarios;
    private final DateTimeFormatter formatador =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(ZoneId.systemDefault());

    public MenuMinhasInteracoes(Scanner scanner,
                                GerenciadorPerguntas gerenciadorPerguntas,
                                GerenciadorRespostas gerenciadorRespostas,
                                ArquivoUsuario arquivoUsuarios) {
        this.scanner = scanner;
        this.gerenciadorPerguntas = gerenciadorPerguntas;
        this.gerenciadorRespostas = gerenciadorRespostas;
        this.arquivoUsuarios = arquivoUsuarios;
    }

    // ---------- (C) MINHAS RESPOSTAS ----------

    public void minhasRespostas(Usuario logado) throws Exception {
        while (true) {
            // recarrega a cada volta para refletir arquivamentos
            List<Resposta> respostas = gerenciadorRespostas.listarRespostasDoUsuario(logado.idUsuario);

            cabecalho("Início > Minha área > Minhas respostas");
            if (respostas.isEmpty()) {
                mensagem("Você ainda não escreveu respostas.");
                return;
            }

            for (int i = 0; i < respostas.size(); i++) {
                Resposta r = respostas.get(i);
                Pergunta p = gerenciadorPerguntas.buscarPergunta(r.idPergunta);
                System.out.println("(" + (i + 1) + ") " + r.resposta);
                System.out.println("    Pergunta: " + (p == null ? "(não encontrada)" : p.pergunta));
                System.out.println("    Respondido em " + data(r.criacao)
                        + " | Nota: " + r.nota + "  (" + r.avaliacoes + " avaliações)\n");
            }
            System.out.println("(R) Retornar ao menu anterior\n");

            String op = lerOpcao();
            if (op.equals("R")) {
                return;
            }
            int n = converterNumero(op);
            if (n < 1 || n > respostas.size()) {
                mensagem("Opção inválida.");
                continue;
            }

            Resposta selecionada = respostas.get(n - 1);
            Pergunta pergunta = gerenciadorPerguntas.buscarPergunta(selecionada.idPergunta);
            if (pergunta == null) {
                mensagem("A pergunta desta resposta não foi encontrada.");
                continue;
            }
            // a resposta é do próprio usuário, então o MenuResposta só oferece "Arquivar resposta"
            new MenuResposta(scanner, gerenciadorRespostas).exibir(
                    logado, pergunta, nomeUsuario(pergunta.idUsuario),
                    selecionada, logado.nome,
                    "Início > Minha área > Minhas respostas", false);
        }
    }

    // ---------- (D) MINHAS AVALIAÇÕES ----------

    public void minhasAvaliacoes(Usuario logado) throws Exception {
        while (true) {
            List<Avaliacao> avaliacoes = gerenciadorRespostas.listarAvaliacoesDoUsuario(logado.idUsuario);

            cabecalho("Início > Minha área > Minhas avaliações");
            if (avaliacoes.isEmpty()) {
                mensagem("Você ainda não avaliou nenhuma resposta.");
                return;
            }

            for (int i = 0; i < avaliacoes.size(); i++) {
                Avaliacao a = avaliacoes.get(i);
                Resposta r = gerenciadorRespostas.buscarResposta(a.idResposta);
                System.out.println("(" + (i + 1) + ") " + r.resposta);
                System.out.println("    Respondido por " + nomeUsuario(r.idUsuario)
                        + " | Sua avaliação: " + descricao(a) + "\n");
            }
            System.out.println("(R) Retornar ao menu anterior\n");

            String op = lerOpcao();
            if (op.equals("R")) {
                return;
            }
            int n = converterNumero(op);
            if (n < 1 || n > avaliacoes.size()) {
                mensagem("Opção inválida.");
                continue;
            }
            exibirAvaliacao(logado, avaliacoes.get(n - 1));
        }
    }

    private void exibirAvaliacao(Usuario logado, Avaliacao avaliacao) throws Exception {
        while (true) {
            Resposta resposta = gerenciadorRespostas.buscarResposta(avaliacao.idResposta);
            Pergunta pergunta = resposta == null ? null : gerenciadorPerguntas.buscarPergunta(resposta.idPergunta);
            if (resposta == null || pergunta == null) {
                mensagem("Esta avaliação não está mais disponível.");
                return;
            }

            cabecalho("Início > Minha área > Minhas avaliações > Avaliação");
            System.out.println(pergunta.pergunta);
            System.out.println("Criada por " + nomeUsuario(pergunta.idUsuario) + " em " + data(pergunta.criacao) + "\n");
            System.out.println("RESPOSTA");
            System.out.println("--------\n");
            System.out.println(resposta.resposta);
            System.out.println("Respondido por " + nomeUsuario(resposta.idUsuario) + " em " + data(resposta.criacao));
            System.out.println("Nota: " + resposta.nota + "  (" + resposta.avaliacoes + " avaliações)\n");
            System.out.println("SUA AVALIAÇÃO: " + descricao(avaliacao).toUpperCase() + "\n");
            System.out.println("(A) Cancelar avaliação");
            System.out.println("\n(R) Retornar ao menu anterior\n");

            String op = lerOpcao();
            if (op.equals("R")) {
                return;
            }
            if (!op.equals("A")) {
                mensagem("Opção inválida.");
                continue;
            }

            System.out.print("Confirmar cancelamento da avaliação? (S/N): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("S")) {
                continue;
            }
            if (gerenciadorRespostas.cancelarAvaliacao(logado.idUsuario, avaliacao.idAvaliacao)) {
                mensagem("Avaliação cancelada. A nota da resposta foi ajustada.");
            } else {
                mensagem("Não foi possível cancelar a avaliação.");
            }
            return;
        }
    }

    // ---------- utilitários ----------

    private String descricao(Avaliacao a) {
        return a.avaliacao ? "concordou (positiva)" : "discordou (negativa)";
    }

    private String nomeUsuario(int idUsuario) throws Exception {
        Usuario u = arquivoUsuarios.read(idUsuario);
        return u == null ? "Usuário não encontrado" : u.nome;
    }

    private String data(long millis) {
        return formatador.format(Instant.ofEpochMilli(millis));
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
        System.out.print("Opção: ");
        return scanner.nextLine().trim().toUpperCase();
    }

    private void mensagem(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("Pressione ENTER para continuar...");
        scanner.nextLine();
    }
}
