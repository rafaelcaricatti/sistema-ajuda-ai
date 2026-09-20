import aed3.RegistroHashExtensivel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParEmailIdUsuario implements RegistroHashExtensivel<ParEmailIdUsuario> {

    private int hashEmail;
    private int idUsuario;
    private final short TAMANHO = 8;

    public ParEmailIdUsuario() {
        this.hashEmail = 0;
        this.idUsuario = -1;
    }

    public ParEmailIdUsuario(String email, int idUsuario) {
        this.hashEmail = email.hashCode();
        this.idUsuario = idUsuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    @Override
    public int hashCode() {
        return hashEmail;
    }

    public short size() {
        return this.TAMANHO;
    }

    public String toString() {
        return "(" + this.hashEmail + ";" + this.idUsuario + ")";
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.hashEmail);
        dos.writeInt(this.idUsuario);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.hashEmail = dis.readInt();
        this.idUsuario = dis.readInt();
    }
}
