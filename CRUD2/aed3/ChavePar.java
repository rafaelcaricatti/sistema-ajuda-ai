public class ChavePar implements Comparable<ChavePar> {
    private final int primeiro;
    private final int segundo;

    public ChavePar(int primeiro, int segundo) {
        this.primeiro = primeiro;
        this.segundo = segundo;
    }

    public int getPrimeiro() {
        return primeiro;
    }

    public int getSegundo() {
        return segundo;
    }

    @Override
    public int compareTo(ChavePar outra) {
        int comparacao = Integer.compare(primeiro, outra.primeiro);
        if (comparacao != 0) {
            return comparacao;
        }
        return Integer.compare(segundo, outra.segundo);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof ChavePar)) {
            return false;
        }
        ChavePar outra = (ChavePar) objeto;
        return primeiro == outra.primeiro && segundo == outra.segundo;
    }

    @Override
    public int hashCode() {
        return 31 * primeiro + segundo;
    }
}