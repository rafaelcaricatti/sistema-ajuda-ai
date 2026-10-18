import aed3.Registro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Resposta implements Registro {

    public int idResposta;
    public int idPergunta;
    public int idUsuario;
    public long criacao;
    public String resposta;
    public short nota;
    public short avaliacoes;
    public boolean ativa;

    public Resposta() {
        this(-1, -1, -1, System.currentTimeMillis(), "", (short) 0, (short) 0, true);
    }

    public Resposta(int idPergunta, int idUsuario, String resposta) {
        this(-1, idPergunta, idUsuario, System.currentTimeMillis(), resposta, (short) 0, (short) 0, true);
    }

    public Resposta(int idResposta, int idPergunta, int idUsuario, long criacao,
                    String resposta, short nota, short avaliacoes, boolean ativa) {
        this.idResposta = idResposta;
        this.idPergunta = idPergunta;
        this.idUsuario = idUsuario;
        this.criacao = criacao;
        this.resposta = resposta;
        this.nota = nota;
        this.avaliacoes = avaliacoes;
        this.ativa = ativa;
    }

    @Override
    public void setId(int id) {
        this.idResposta = id;
    }

    @Override
    public int getId() {
        return idResposta;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idResposta);
        dos.writeInt(idPergunta);
        dos.writeInt(idUsuario);
        dos.writeLong(criacao);
        dos.writeUTF(resposta);
        dos.writeShort(nota);
        dos.writeShort(avaliacoes);
        dos.writeBoolean(ativa);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        idResposta = dis.readInt();
        idPergunta = dis.readInt();
        idUsuario = dis.readInt();
        criacao = dis.readLong();
        resposta = dis.readUTF();
        nota = dis.readShort();
        avaliacoes = dis.readShort();
        ativa = dis.readBoolean();
    }
}