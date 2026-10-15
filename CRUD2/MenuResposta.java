import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


// Tela de UMA resposta. Fica em classe própria para não gerar conflito de merge
// com quem mexe no InterfaceSistema. Quem lista as respostas só precisa chamar:
// new MenuResposta(scanner, gerenciadorRespostas, nomeDoAutor).exibir(usuarioLogado, resposta, "Buscar perguntas > ... > Pergunta");

public class MenuResposta {

    private final Scanner scanner;
    private final GerenciadorRespostas gerenciador;
    private final DateTimeFormatter formatador =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(ZoneId.systemDefault());

    public MenuResposta(Scanner scanner, GerenciadorRespostas gerenciador) {
        this.scanner = scanner;
        this.gerenciador = gerenciador;
    }

    public void exibir(Usuario logado, Resposta resposta, String nomeAutor, String caminho) throws Exception {
        boolean voltar = false;

        while (!voltar) {
            // recarrega do arquivo para sempre mostrar nota/avaliações atuais
            resposta = gerenciador.buscarResposta(resposta.idResposta);
            if (resposta == null || !resposta.ativa) {
                System.out.println("\nEsta resposta não está mais disponível.");
                return;
            }

            boolean propria = resposta.idUsuario == logado.idUsuario;

            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("------------");
            System.out.println("> " + caminho + " > Resposta\n");
            System.out.println("RESPOSTA");
            System.out.println("--------\n");
            System.out.println(resposta.resposta);
            System.out.println("Respondido por " + (propria ? "você" : nomeAutor)
                    + " em " + formatador.format(Instant.ofEpochMilli(resposta.criacao)));
            System.out.println("Nota: " + resposta.nota + "  (" + resposta.avaliacoes + " avaliações)\n");

            if (propria) {
                // autor da resposta: sem botões de voto, só arquivar
                System.out.println("(A) Arquivar resposta");
            } else {
                System.out.println("(A) Concordar com a resposta");
                System.out.println("(B) Discordar da resposta");
            }
            System.out.println("\n(R) Retornar ao menu anterior\n");
            System.out.print("Opção: ");
            String op = scanner.nextLine().trim().toUpperCase();

            if (op.equals("R")) {
                voltar = true;
            } else if (propria && op.equals("A")) {
                System.out.print("Confirmar arquivamento? (S/N): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("S")) {
                    if (gerenciador.arquivarResposta(logado.idUsuario, resposta.idResposta)) {
                        System.out.println("\nResposta arquivada com sucesso.");
                        return;
                    }
                    System.out.println("\nNão foi possível arquivar a resposta.");
                }
            } else if (!propria && (op.equals("A") || op.equals("B"))) {
                GerenciadorRespostas.ResultadoVoto r =
                        gerenciador.votar(logado.idUsuario, resposta.idResposta, op.equals("A"));
                switch (r) {
                    case OK: System.out.println("\nAvaliação registrada."); break;
                    case JA_VOTOU: System.out.println("\nVocê já avaliou esta resposta."); break;
                    case PROPRIA_RESPOSTA: System.out.println("\nVocê não pode avaliar a sua própria resposta."); break;
                    default: System.out.println("\nNão foi possível avaliar esta resposta.");
                }
            } else {
                System.out.println("\nOpção inválida.");
            }
        }
    }
}
