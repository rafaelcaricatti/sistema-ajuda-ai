import aed3.Registro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Pergunta implements Registro {

    public int idPergunta;
    public int idUsuario;
    public long criacao;
    public long alteracao;
    public short nota;
    public String pergunta;
    public String palavrasChave;
    public boolean ativa;

    public Pergunta() {
        this(-1, -1, "", "");
    }

    public Pergunta(int idUsuario, String pergunta, String palavrasChave) {
        this(-1, idUsuario, pergunta, palavrasChave);
    }

    public Pergunta(int idPergunta, int idUsuario, String pergunta, String palavrasChave) {
        this.idPergunta = idPergunta;
        this.idUsuario = idUsuario;
        long agora = System.currentTimeMillis();
        this.criacao = agora;
        this.alteracao = agora;
        this.nota = 0;
        this.pergunta = pergunta;
        this.palavrasChave = palavrasChave;
        this.ativa = true;
    }

    public void setId(int id) {
        this.idPergunta = id;
    }

    public int getId() {
        return idPergunta;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    // arquivamento é definitivo, por isso não existe um método pra reativar
    public void arquivar() {
        this.ativa = false;
    }

    public String toString() {
        return "\nID.............: " + this.idPergunta +
               "\nID Usuário.....: " + this.idUsuario +
               "\nCriação........: " + this.criacao +
               "\nAlteração......: " + this.alteracao +
               "\nNota...........: " + this.nota +
               "\nAtiva..........: " + this.ativa +
               "\nPergunta.......: " + this.pergunta +
               "\nPalavras-chave.: " + this.palavrasChave;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.idPergunta);
        dos.writeInt(this.idUsuario);
        dos.writeLong(this.criacao);
        dos.writeLong(this.alteracao);
        dos.writeShort(this.nota);
        dos.writeUTF(this.pergunta);
        dos.writeUTF(this.palavrasChave);
        dos.writeBoolean(this.ativa);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.idPergunta = dis.readInt();
        this.idUsuario = dis.readInt();
        this.criacao = dis.readLong();
        this.alteracao = dis.readLong();
        this.nota = dis.readShort();
        this.pergunta = dis.readUTF();
        this.palavrasChave = dis.readUTF();
        this.ativa = dis.readBoolean();
    }
}
