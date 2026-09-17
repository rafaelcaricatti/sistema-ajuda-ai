import aed3.Arquivo;

public class TesteCRUD {

    public static void main(String[] args) throws Exception {
        testarUsuario();
        System.out.println();
        testarPergunta();
    }

    static void testarUsuario() throws Exception {
        System.out.println("===== TESTE Usuario =====");
        Arquivo<Usuario> arquivoUsuarios = new Arquivo<>("usuarios", Usuario.class.getConstructor());

        Usuario u1 = new Usuario("Rafael Silva", "rafael@puc.br", "hashSenha123",
                "Nome do seu primeiro pet?", "hashRespostaCachorro");
        int id1 = arquivoUsuarios.create(u1);
        System.out.println("Criado -> ID " + id1);

        Usuario u2 = new Usuario("Maria Souza", "maria@puc.br", "hashSenha456",
                "Cidade onde nasceu?", "hashRespostaCidade");
        int id2 = arquivoUsuarios.create(u2);
        System.out.println("Criado -> ID " + id2);

        System.out.println("Leitura ID " + id1 + ": " + arquivoUsuarios.read(id1));

        // aq update que cabe no espaço original
        Usuario lido1 = arquivoUsuarios.read(id1);
        lido1.email = "rafael@puc.mg";
        arquivoUsuarios.update(lido1);
        System.out.println("Após update simples: " + arquivoUsuarios.read(id1));

        // aq o update que cresce além do espaço original
        Usuario lido2 = arquivoUsuarios.read(id2);
        lido2.nome = "Maria Fernanda de Souza Albuquerque Nascimento";
        arquivoUsuarios.update(lido2);
        System.out.println("Após update que cresce: " + arquivoUsuarios.read(id2));

        arquivoUsuarios.delete(id1);
        System.out.println("Após delete do ID " + id1 + " (esperado null): " + arquivoUsuarios.read(id1));
        System.out.println("ID " + id2 + " continua acessível: " + arquivoUsuarios.read(id2));

        arquivoUsuarios.close();
    }

    static void testarPergunta() throws Exception {
        System.out.println("===== TESTE Pergunta =====");
        Arquivo<Pergunta> arquivoPerguntas = new Arquivo<>("perguntas", Pergunta.class.getConstructor());

        Pergunta p1 = new Pergunta(1, "É seguro comer pão mofado, se cortar a parte mofada fora?",
                "pão;mofado;saúde");
        int idP1 = arquivoPerguntas.create(p1);
        System.out.println("Criada -> ID " + idP1);

        Pergunta p2 = new Pergunta(2, "Qual linguagem recomendam para quem está começando a programar?",
                "programação;linguagem");
        int idP2 = arquivoPerguntas.create(p2);
        System.out.println("Criada -> ID " + idP2);

        System.out.println("Leitura ID " + idP1 + ": " + arquivoPerguntas.read(idP1));

        // aq o update que cresce bastante (palavras chave bem maiores)
        Pergunta lida1 = arquivoPerguntas.read(idP1);
        lida1.palavrasChave = "pão;mofo;fungos;alimentação;segurança alimentar;saúde;intoxicação";
        arquivoPerguntas.update(lida1);
        System.out.println("Após update que cresce: " + arquivoPerguntas.read(idP1));

        // arquivamento: não é exclusão, o registro continua existindo com ativa = false
        Pergunta lida2 = arquivoPerguntas.read(idP2);
        lida2.arquivar();
        arquivoPerguntas.update(lida2);
        System.out.println("Após arquivar ID " + idP2 + ": " + arquivoPerguntas.read(idP2));

        arquivoPerguntas.close();
    }
}
