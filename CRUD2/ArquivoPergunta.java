import aed3.Arquivo;
import aed3.ArvoreBMais;
import aed3.ParDataId;
import aed3.ParIdId;

import java.io.File;
import java.util.ArrayList;

public class ArquivoPergunta extends Arquivo<Pergunta> {

    private final ArvoreBMais<ParIdId> relUsuarioPergunta;
    private final ArvoreBMais<ParDataId> indiceDataPergunta;

    public ArquivoPergunta() throws Exception {
        super("perguntas", Pergunta.class.getConstructor());

        File pasta = new File("./dados/perguntas");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        relUsuarioPergunta = new ArvoreBMais<>(
                ParIdId.class.getConstructor(),
                5,
                "./dados/perguntas/relUsuarioPergunta.db"
        );

        indiceDataPergunta = new ArvoreBMais<>(
                ParDataId.class.getConstructor(),
                5,
                "./dados/perguntas/indiceDataPergunta.db"
        );

        if (indiceDataPergunta.empty()) {
            reconstruirIndiceData();
        }
    }

    private void reconstruirIndiceData() throws Exception {
        for (ParIdId relacao : relUsuarioPergunta.read(null)) {
            Pergunta pergunta = super.read(relacao.getId2());
            if (pergunta != null) {
                indiceDataPergunta.create(
                        new ParDataId(pergunta.criacao, pergunta.idPergunta)
                );
            }
        }
    }

    @Override
    public int create(Pergunta pergunta) throws Exception {
        int id = super.create(pergunta);

        boolean criouRelacao = relUsuarioPergunta.create(
                new ParIdId(pergunta.idUsuario, id)
        );

        boolean criouIndiceData = indiceDataPergunta.create(
                new ParDataId(pergunta.criacao, id)
        );

        if (!criouRelacao || !criouIndiceData) {
            throw new Exception("Não foi possível atualizar os índices da pergunta.");
        }

        return id;
    }

    @Override
    public boolean update(Pergunta perguntaAtualizada) throws Exception {
        Pergunta perguntaAntiga = super.read(perguntaAtualizada.idPergunta);

        if (perguntaAntiga == null) {
            return false;
        }

        if (perguntaAntiga.idUsuario != perguntaAtualizada.idUsuario) {
            relUsuarioPergunta.delete(
                    new ParIdId(
                            perguntaAntiga.idUsuario,
                            perguntaAtualizada.idPergunta
                    )
            );

            relUsuarioPergunta.create(
                    new ParIdId(
                            perguntaAtualizada.idUsuario,
                            perguntaAtualizada.idPergunta
                    )
            );
        }

        if (perguntaAntiga.criacao != perguntaAtualizada.criacao) {
            indiceDataPergunta.delete(
                    new ParDataId(
                            perguntaAntiga.criacao,
                            perguntaAtualizada.idPergunta
                    )
            );

            indiceDataPergunta.create(
                    new ParDataId(
                            perguntaAtualizada.criacao,
                            perguntaAtualizada.idPergunta
                    )
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

        relUsuarioPergunta.delete(
                new ParIdId(pergunta.idUsuario, id)
        );

        indiceDataPergunta.delete(
                new ParDataId(pergunta.criacao, id)
        );

        return super.delete(id);
    }

    public Pergunta[] readAllByUsuario(int idUsuario) throws Exception {
        ArrayList<Pergunta> perguntas = new ArrayList<>();
        ArrayList<ParIdId> relacoes = relUsuarioPergunta.read(
                new ParIdId(idUsuario, -1)
        );

        for (ParIdId relacao : relacoes) {
            Pergunta pergunta = super.read(relacao.getId2());

            if (pergunta != null) {
                perguntas.add(pergunta);
            }
        }

        return perguntas.toArray(new Pergunta[0]);
    }

    public Pergunta[] readAllByData() throws Exception {
        ArrayList<Pergunta> perguntas = new ArrayList<>();

        for (ParDataId par : indiceDataPergunta.read(null)) {
            Pergunta pergunta = super.read(par.getId());

            if (pergunta != null && pergunta.ativa) {
                perguntas.add(pergunta);
            }
        }

        return perguntas.toArray(new Pergunta[0]);
    }
}
