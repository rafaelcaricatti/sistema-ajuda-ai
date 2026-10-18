package aed3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParDataId implements InterfaceArvoreBMais<ParDataId> {

    private long data;
    private int id;

    public ParDataId() {
        this(0L, -1);
    }

    public ParDataId(long data, int id) {
        this.data = data;
        this.id = id;
    }

    public long getData() {
        return data;
    }

    public int getId() {
        return id;
    }

    @Override
    public short size() {
        return 12;
    }

    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(data);
        dos.writeInt(id);
        return baos.toByteArray();
    }

    @Override
    public void deserialize(byte[] ba) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba));
        data = dis.readLong();
        id = dis.readInt();
    }

    @Override
    public int compareTo(ParDataId outro) {
        int comparacaoData = Long.compare(outro.data, this.data);
        if (comparacaoData != 0) {
            return comparacaoData;
        }
        return Integer.compare(this.id, outro.id);
    }

    @Override
    public ParDataId clone() {
        return new ParDataId(data, id);
    }

    @Override
    public String toString() {
        return data + ";" + id;
    }
}
