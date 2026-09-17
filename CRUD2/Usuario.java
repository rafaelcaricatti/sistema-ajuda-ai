import aed3.Registro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Usuario implements Registro {

    public int idUsuario;
    public String nome;
    public String email;
    public String hashSenha;
    public String perguntaSecreta;
    public String hashRespostaSecreta;

    public Usuario() {
        this(-1, "", "", "", "", "");
    }

    public Usuario(String nome, String email, String hashSenha, String perguntaSecreta, String hashRespostaSecreta) {
        this(-1, nome, email, hashSenha, perguntaSecreta, hashRespostaSecreta);
    }

    public Usuario(int idUsuario, String nome, String email, String hashSenha, String perguntaSecreta, String hashRespostaSecreta) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.hashSenha = hashSenha;
        this.perguntaSecreta = perguntaSecreta;
        this.hashRespostaSecreta = hashRespostaSecreta;
    }

    public void setId(int id) {
        this.idUsuario = id;
    }

    public int getId() {
        return idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public String toString() {
        return "\nID.................: " + this.idUsuario +
               "\nNome...............: " + this.nome +
               "\nEmail..............: " + this.email +
               "\nPergunta secreta...: " + this.perguntaSecreta;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.idUsuario);
        dos.writeUTF(this.nome);
        dos.writeUTF(this.email);
        dos.writeUTF(this.hashSenha);
        dos.writeUTF(this.perguntaSecreta);
        dos.writeUTF(this.hashRespostaSecreta);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.idUsuario = dis.readInt();
        this.nome = dis.readUTF();
        this.email = dis.readUTF();
        this.hashSenha = dis.readUTF();
        this.perguntaSecreta = dis.readUTF();
        this.hashRespostaSecreta = dis.readUTF();
    }
}
