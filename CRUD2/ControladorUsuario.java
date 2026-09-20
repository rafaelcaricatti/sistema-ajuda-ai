public class ControladorUsuario {

    private final ArquivoUsuario arquivoUsuarios;

    public ControladorUsuario() throws Exception {
        this.arquivoUsuarios = new ArquivoUsuario();
    }

    public Usuario cadastrar(String nome, String email, String senha,
                              String perguntaSecreta, String respostaSecreta) throws Exception {
        if (arquivoUsuarios.existeEmail(email))
            throw new Exception("Já existe um usuário cadastrado com o email " + email);

        String hashSenha = Seguranca.gerarHash(senha);
        String hashResposta = Seguranca.gerarHashRespostaSecreta(respostaSecreta);

        Usuario usuario = new Usuario(nome, email, hashSenha, perguntaSecreta, hashResposta);
        arquivoUsuarios.create(usuario);
        return usuario;
    }

    public Usuario login(String email, String senha) throws Exception {
        Usuario usuario = arquivoUsuarios.buscarPorEmail(email);
        if (usuario == null)
            return null;

        String hashSenha = Seguranca.gerarHash(senha);
        if (!usuario.hashSenha.equals(hashSenha))
            return null;

        return usuario;
    }

    public String consultarPerguntaSecreta(String email) throws Exception {
        Usuario usuario = arquivoUsuarios.buscarPorEmail(email);
        return usuario == null ? null : usuario.perguntaSecreta;
    }

    public boolean recuperarSenha(String email, String respostaSecreta, String novaSenha) throws Exception {
        Usuario usuario = arquivoUsuarios.buscarPorEmail(email);
        if (usuario == null)
            return false;

        String hashResposta = Seguranca.gerarHashRespostaSecreta(respostaSecreta);
        if (!usuario.hashRespostaSecreta.equals(hashResposta))
            return false;

        usuario.hashSenha = Seguranca.gerarHash(novaSenha);
        return arquivoUsuarios.update(usuario);
    }

    public ArquivoUsuario getArquivoUsuarios() {
        return arquivoUsuarios;
    }
}
