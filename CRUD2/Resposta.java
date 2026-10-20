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
        this(-1, -1, "");
    }

    // Construtor usado ao responder: tudo que não é digitado pelo usuário é preenchido aqui.
    public Resposta(int idPergunta, int idUsuario, String resposta) {
        this.idResposta = -1;
        this.idPergunta = idPergunta;
        this.idUsuario = idUsuario;
        this.criacao = System.currentTimeMillis();
        this.resposta = resposta;
        this.nota = 0;
        this.avaliacoes = 0;
        this.ativa = true;
    }

    public void setId(int id) {
        this.idResposta = id;
    }

    public int getId() {
        return idResposta;
    }

    // exclusão lógica: a resposta continua no arquivo, só deixa de aparecer
    public void arquivar() {
        this.ativa = false;
    }

    public String toString() {
        return "\nID.............: " + this.idResposta +
               "\nID Pergunta....: " + this.idPergunta +
               "\nID Usuário.....: " + this.idUsuario +
               "\nCriação........: " + this.criacao +
               "\nNota...........: " + this.nota +
               "\nAvaliações.....: " + this.avaliacoes +
               "\nAtiva..........: " + this.ativa +
               "\nResposta.......: " + this.resposta;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.idResposta);
        dos.writeInt(this.idPergunta);
        dos.writeInt(this.idUsuario);
        dos.writeLong(this.criacao);
        dos.writeUTF(this.resposta);
        dos.writeShort(this.nota);
        dos.writeShort(this.avaliacoes);
        dos.writeBoolean(this.ativa);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] b) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
        this.idResposta = dis.readInt();
        this.idPergunta = dis.readInt();
        this.idUsuario = dis.readInt();
        this.criacao = dis.readLong();
        this.resposta = dis.readUTF();
        this.nota = dis.readShort();
        this.avaliacoes = dis.readShort();
        this.ativa = dis.readBoolean();
    }
}
