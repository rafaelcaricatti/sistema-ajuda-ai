import aed3.Arquivo;
import aed3.ParIdId;

import java.util.ArrayList;

public class ArquivoResposta extends Arquivo<Resposta> {

    // [idPergunta, idResposta] -> respostas de uma pergunta
    private final aed3.ArvoreBMais<ParIdId> relPerguntaResposta;
    // [idUsuario, idResposta] -> respostas escritas por um usuário
    private final aed3.ArvoreBMais<ParIdId> relUsuarioResposta;

    public ArquivoResposta() throws Exception {
        super("respostas", Resposta.class.getConstructor());

        relPerguntaResposta = new aed3.ArvoreBMais<>(
                ParIdId.class.getConstructor(), 5, "./dados/respostas/relPerguntaResposta.db");
        relUsuarioResposta = new aed3.ArvoreBMais<>(
                ParIdId.class.getConstructor(), 5, "./dados/respostas/relUsuarioResposta.db");
    }

    @Override
    public int create(Resposta r) throws Exception {
        int id = super.create(r);
        boolean ok1 = relPerguntaResposta.create(new ParIdId(r.idPergunta, id));
        boolean ok2 = relUsuarioResposta.create(new ParIdId(r.idUsuario, id));
        if (!ok1 || !ok2) {
            throw new Exception("Não foi possível criar os relacionamentos da resposta.");
        }
        return id;
    }

    // Arquivar é só um update (ativa=false); as árvores continuam iguais, como no TP1.
    // Só mexemos nelas se pergunta/usuário forem trocados (não acontece pela interface).
    @Override
    public boolean update(Resposta nova) throws Exception {
        Resposta antiga = super.read(nova.idResposta);
        if (antiga == null) {
            return false;
        }
        if (antiga.idPergunta != nova.idPergunta) {
            relPerguntaResposta.delete(new ParIdId(antiga.idPergunta, nova.idResposta));
            relPerguntaResposta.create(new ParIdId(nova.idPergunta, nova.idResposta));
        }
        if (antiga.idUsuario != nova.idUsuario) {
            relUsuarioResposta.delete(new ParIdId(antiga.idUsuario, nova.idResposta));
            relUsuarioResposta.create(new ParIdId(nova.idUsuario, nova.idResposta));
        }
        return super.update(nova);
    }

    @Override
    public boolean delete(int id) throws Exception {
        Resposta r = super.read(id);
        if (r == null) {
            return false;
        }
        relPerguntaResposta.delete(new ParIdId(r.idPergunta, id));
        relUsuarioResposta.delete(new ParIdId(r.idUsuario, id));
        return super.delete(id);
    }

    public Resposta[] readAllByPergunta(int idPergunta) throws Exception {
        ArrayList<Resposta> lista = new ArrayList<>();
        for (ParIdId par : relPerguntaResposta.read(new ParIdId(idPergunta, -1))) {
            Resposta r = super.read(par.getId2());
            if (r != null) {
                lista.add(r);
            }
        }
        return lista.toArray(new Resposta[0]);
    }

    public Resposta[] readAllByUsuario(int idUsuario) throws Exception {
        ArrayList<Resposta> lista = new ArrayList<>();
        for (ParIdId par : relUsuarioResposta.read(new ParIdId(idUsuario, -1))) {
            Resposta r = super.read(par.getId2());
            if (r != null) {
                lista.add(r);
            }
        }
        return lista.toArray(new Resposta[0]);
    }
}
