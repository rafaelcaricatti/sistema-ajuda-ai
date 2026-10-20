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
    public boolean avaliacao; // true = concordou, false = discordou

    public Avaliacao() {
        this(-1, -1, false);
    }

    public Avaliacao(int idUsuario, int idResposta, boolean avaliacao) {
        this.idAvaliacao = -1;
        this.idUsuario = idUsuario;
        this.idResposta = idResposta;
        this.avaliacao = avaliacao;
    }

    public void setId(int id) {
        this.idAvaliacao = id;
    }

    public int getId() {
        return idAvaliacao;
    }

    public String toString() {
        return "\nID.............: " + this.idAvaliacao +
               "\nID Usuário.....: " + this.idUsuario +
               "\nID Resposta....: " + this.idResposta +
               "\nAvaliação......: " + (this.avaliacao ? "positiva" : "negativa");
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.idAvaliacao);
        dos.writeInt(this.idUsuario);
        dos.writeInt(this.idResposta);
        dos.writeBoolean(this.avaliacao);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] b) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
        this.idAvaliacao = dis.readInt();
        this.idUsuario = dis.readInt();
        this.idResposta = dis.readInt();
        this.avaliacao = dis.readBoolean();
    }
}
