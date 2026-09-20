import aed3.Arquivo;
import aed3.HashExtensivel;

public class ArquivoUsuario extends Arquivo<Usuario> {

    private HashExtensivel<ParEmailIdUsuario> indiceEmail;

    public ArquivoUsuario() throws Exception {
        super("usuarios", Usuario.class.getConstructor());
        indiceEmail = new HashExtensivel<>(
                ParEmailIdUsuario.class.getConstructor(),
                4,
                ".\\dados\\usuarios\\usuarios.email.d.db",
                ".\\dados\\usuarios\\usuarios.email.c.db"
        );
    }

    public boolean existeEmail(String email) throws Exception {
        return buscarPorEmail(email) != null;
    }

    public Usuario buscarPorEmail(String email) throws Exception {
        ParEmailIdUsuario par = indiceEmail.read(email.hashCode());
        if (par == null)
            return null;
        return read(par.getIdUsuario());
    }

    @Override
    public int create(Usuario usuario) throws Exception {
        if (existeEmail(usuario.email))
            throw new Exception("Já existe um usuário cadastrado com este email");
        int id = super.create(usuario);
        indiceEmail.create(new ParEmailIdUsuario(usuario.email, id));
        return id;
    }

    @Override
    public boolean update(Usuario novoUsuario) throws Exception {
        Usuario atual = read(novoUsuario.idUsuario);
        if (atual == null)
            return false;

        if (!atual.email.equals(novoUsuario.email)) {
            if (existeEmail(novoUsuario.email))
                throw new Exception("Já existe um usuário cadastrado com este email");
            indiceEmail.delete(atual.email.hashCode());
            indiceEmail.create(new ParEmailIdUsuario(novoUsuario.email, novoUsuario.idUsuario));
        }

        return super.update(novoUsuario);
    }

    @Override
    public boolean delete(int id) throws Exception {
        Usuario atual = read(id);
        if (atual == null)
            return false;
        indiceEmail.delete(atual.email.hashCode());
        return super.delete(id);
    }

    @Override
    public void close() throws Exception {
        super.close();
        indiceEmail.close();
    }
}
