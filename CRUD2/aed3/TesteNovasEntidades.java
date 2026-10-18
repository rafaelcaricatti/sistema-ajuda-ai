public class TesteNovasEntidades {

    public static void main(String[] args) throws Exception {
        testarResposta();
        testarAvaliacao();
        System.out.println("Teste das entidades OK");
    }

    private static void testarResposta() throws Exception {
        Resposta original = new Resposta(14, 8, 5, 123456789L,
                "Resposta de teste", (short) 3, (short) 7, false);
        Resposta copia = new Resposta();
        copia.fromByteArray(original.toByteArray());

        if (copia.idResposta != 14 || copia.idPergunta != 8 || copia.idUsuario != 5
                || copia.criacao != 123456789L || !"Resposta de teste".equals(copia.resposta)
                || copia.nota != 3 || copia.avaliacoes != 7 || copia.ativa) {
            throw new AssertionError("A serialização de Resposta perdeu algum campo");
        }
    }

    private static void testarAvaliacao() throws Exception {
        Avaliacao original = new Avaliacao(27, 9, 14, true);
        Avaliacao copia = new Avaliacao();
        copia.fromByteArray(original.toByteArray());

        if (copia.idAvaliacao != 27 || copia.idUsuario != 9 || copia.idResposta != 14
                || !copia.avaliacao) {
            throw new AssertionError("A serialização de Avaliacao perdeu algum campo");
        }
    }
}