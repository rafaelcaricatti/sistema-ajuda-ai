import aed3.Arquivo;
import aed3.ParIdId;

import java.util.ArrayList;

public class ArquivoAvaliacao extends Arquivo<Avaliacao> {

    // [idUsuario, idAvaliacao] -> avaliações feitas por um usuário
    private final aed3.ArvoreBMais<ParIdId> relUsuarioAvaliacao;
    // [idResposta, idAvaliacao] -> avaliações recebidas por uma resposta
    private final aed3.ArvoreBMais<ParIdId> relRespostaAvaliacao;

    public ArquivoAvaliacao() throws Exception {
        super("avaliacoes", Avaliacao.class.getConstructor());

        relUsuarioAvaliacao = new aed3.ArvoreBMais<>(
                ParIdId.class.getConstructor(), 5, "./dados/avaliacoes/relUsuarioAvaliacao.db");
        relRespostaAvaliacao = new aed3.ArvoreBMais<>(
                ParIdId.class.getConstructor(), 5, "./dados/avaliacoes/relRespostaAvaliacao.db");
    }

    @Override
    public int create(Avaliacao a) throws Exception {
        int id = super.create(a);
        boolean ok1 = relUsuarioAvaliacao.create(new ParIdId(a.idUsuario, id));
        boolean ok2 = relRespostaAvaliacao.create(new ParIdId(a.idResposta, id));
        if (!ok1 || !ok2) {
            throw new Exception("Não foi possível criar os relacionamentos da avaliação.");
        }
        return id;
    }

    @Override
    public boolean delete(int id) throws Exception {
        Avaliacao a = super.read(id);
        if (a == null) {
            return false;
        }
        relUsuarioAvaliacao.delete(new ParIdId(a.idUsuario, id));
        relRespostaAvaliacao.delete(new ParIdId(a.idResposta, id));
        return super.delete(id);
    }

    public Avaliacao[] readAllByUsuario(int idUsuario) throws Exception {
        ArrayList<Avaliacao> lista = new ArrayList<>();
        for (ParIdId par : relUsuarioAvaliacao.read(new ParIdId(idUsuario, -1))) {
            Avaliacao a = super.read(par.getId2());
            if (a != null) {
                lista.add(a);
            }
        }
        return lista.toArray(new Avaliacao[0]);
    }

    // Trava anti-voto duplo: devolve a avaliação do usuário para essa resposta, ou null se não votou.
    public Avaliacao buscar(int idUsuario, int idResposta) throws Exception {
        for (Avaliacao a : readAllByUsuario(idUsuario)) {
            if (a.idResposta == idResposta) {
                return a;
            }
        }
        return null;
    }
}
