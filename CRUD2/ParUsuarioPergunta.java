public class ParUsuarioPergunta implements Comparable<ParUsuarioPergunta> {
    private final int idUsuario;
    private final int idPergunta;

    public ParUsuarioPergunta(int idUsuario, int idPergunta) {
        this.idUsuario = idUsuario;
        this.idPergunta = idPergunta;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public int getIdPergunta() {
        return idPergunta;
    }

    @Override
    public int compareTo(ParUsuarioPergunta outro) {
        if (this.idUsuario != outro.idUsuario) {
            return Integer.compare(this.idUsuario, outro.idUsuario);
        }
        return Integer.compare(this.idPergunta, outro.idPergunta);
    }

    @Override
    public String toString() {
        return "(" + idUsuario + ", " + idPergunta + ")";
    }
}
