import aed3.Registro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Avaliacao implements Registro {

    public int idAvaliacao;
    public int idUsuario;
    public int idResposta;
    public boolean avaliacao;

    public Avaliacao() {
        this(-1, -1, -1, false);
    }

    public Avaliacao(int idUsuario, int idResposta, boolean avaliacao) {
        this(-1, idUsuario, idResposta, avaliacao);
    }

    public Avaliacao(int idAvaliacao, int idUsuario, int idResposta, boolean avaliacao) {
        this.idAvaliacao = idAvaliacao;
        this.idUsuario = idUsuario;
        this.idResposta = idResposta;
        this.avaliacao = avaliacao;
    }

    @Override
    public void setId(int id) {
        this.idAvaliacao = id;
    }

    @Override
    public int getId() {
        return idAvaliacao;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idAvaliacao);
        dos.writeInt(idUsuario);
        dos.writeInt(idResposta);
        dos.writeBoolean(avaliacao);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        idAvaliacao = dis.readInt();
        idUsuario = dis.readInt();
        idResposta = dis.readInt();
        avaliacao = dis.readBoolean();
    }
}