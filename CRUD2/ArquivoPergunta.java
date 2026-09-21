import aed3.Arquivo;
import aed3.ParIdId;
import java.io.File;
import java.util.ArrayList;

public class ArquivoPergunta extends Arquivo<Pergunta> {

    private final aed3.ArvoreBMais<ParIdId> relUsuarioPergunta;

    public ArquivoPergunta() throws Exception {
        super("perguntas", Pergunta.class.getConstructor());

        File pasta = new File("./dados/perguntas");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        relUsuarioPergunta = new aed3.ArvoreBMais<>(
                ParIdId.class.getConstructor(),
                5,
                "./dados/perguntas/relUsuarioPergunta.db"
        );
    }

    @Override
    public int create(Pergunta pergunta) throws Exception {
        int id = super.create(pergunta);
        boolean criouRelacao = relUsuarioPergunta.create(new ParIdId(pergunta.idUsuario, id));
        if (!criouRelacao) {
            throw new Exception("Não foi possível criar o relacionamento usuário-pergunta.");
        }
        return id;
    }

    @Override
    public boolean update(Pergunta perguntaAtualizada) throws Exception {
        Pergunta perguntaAntiga = super.read(perguntaAtualizada.idPergunta);
        if (perguntaAntiga == null) {
            return false;
        }

        // IDs não são alterados pela interface, mas mantemos o relacionamento
        // consistente caso o objeto seja alterado indevidamente em outro ponto.
        if (perguntaAntiga.idUsuario != perguntaAtualizada.idUsuario) {
            relUsuarioPergunta.delete(
                    new ParIdId(perguntaAntiga.idUsuario, perguntaAtualizada.idPergunta)
            );
            relUsuarioPergunta.create(
                    new ParIdId(perguntaAtualizada.idUsuario, perguntaAtualizada.idPergunta)
            );
        }

        return super.update(perguntaAtualizada);
    }

    @Override
    public boolean delete(int id) throws Exception {
        Pergunta pergunta = super.read(id);
        if (pergunta == null) {
            return false;
        }

        relUsuarioPergunta.delete(new ParIdId(pergunta.idUsuario, id));
        return super.delete(id);
    }

    public Pergunta[] readAllByUsuario(int idUsuario) throws Exception {
        ArrayList<Pergunta> perguntas = new ArrayList<>();
        ArrayList<ParIdId> relacoes = relUsuarioPergunta.read(new ParIdId(idUsuario, -1));

        for (ParIdId relacao : relacoes) {
            Pergunta pergunta = super.read(relacao.getId2());
            if (pergunta != null) {
                perguntas.add(pergunta);
            }
        }

        return perguntas.toArray(new Pergunta[0]);
    }
}
