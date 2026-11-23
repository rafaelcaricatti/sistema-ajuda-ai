public class ResultadoBusca {

    private final int idPergunta;
    private final float peso;

    public ResultadoBusca(int idPergunta, float peso) {
        this.idPergunta = idPergunta;
        this.peso = peso;
    }

    public int getIdPergunta() {
        return idPergunta;
    }

    public float getPeso() {
        return peso;
    }

    @Override
    public String toString() {
        return String.format("[id=%d, peso=%.4f]", idPergunta, peso);
    }
}
