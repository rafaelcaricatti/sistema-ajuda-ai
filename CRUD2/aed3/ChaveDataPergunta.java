public class ChaveDataPergunta implements Comparable<ChaveDataPergunta> {
    private final long criacao;
    private final int idPergunta;

    public ChaveDataPergunta(long criacao, int idPergunta) {
        this.criacao = criacao;
        this.idPergunta = idPergunta;
    }

    @Override
    public int compareTo(ChaveDataPergunta outra) {
        int comparacao = Long.compare(outra.criacao, criacao);
        if (comparacao != 0) {
            return comparacao;
        }
        return Integer.compare(idPergunta, outra.idPergunta);
    }
}