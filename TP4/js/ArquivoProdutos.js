// CRUD de produtos feito diretamente sobre um vetor de bytes (Int8Array),
// no mesmo modelo do Arquivo.java da disciplina:
//
// [cabeçalho: int ultimoId]
// [registro: byte lápide | short tamanho | dados]...
//
// dados = int id | string nome | string descricao | float preco | int estoque | date dataCadastro
// lápide: ' ' = ativo, '*' = excluído.

const ByteStreamProdutos =
  typeof ByteStream !== "undefined" ? ByteStream : require("./ByteStream.js");

class ArquivoProdutos {
  static TAM_CABECALHO = 4;
  static LAPIDE_ATIVO = 32; // ' '
  static LAPIDE_EXCLUIDO = 42; // '*'

  constructor(bytes) {
    if (bytes && bytes.length >= ArquivoProdutos.TAM_CABECALHO) {
      this.bytes = Int8Array.from(bytes);
    } else {
      this.bytes = new Int8Array(ArquivoProdutos.TAM_CABECALHO);
      this.escrever(ByteStreamProdutos.writeInt(0), 0);
    }
  }

  getBytes() {
    return Int8Array.from(this.bytes);
  }

  ultimoId() {
    return ByteStreamProdutos.readInt(this.bytes, 0);
  }

  // Inclusão: novo id e registro anexado ao fim do vetor
  create(produto) {
    const id = this.ultimoId() + 1;
    this.escrever(ByteStreamProdutos.writeInt(id), 0);
    this.anexar(id, produto);
    return id;
  }

  read(id) {
    const registro = this.localizar(id);
    return registro ? registro.produto : null;
  }

  readAll() {
    return this.listarRegistros()
      .filter((r) => r.ativo)
      .map((r) => r.produto);
  }

  buscarPorNome(texto) {
    const alvo = String(texto).toLowerCase();
    return this.readAll().filter((p) => p.nome.toLowerCase().includes(alvo));
  }

  // Se couber no espaço antigo, reescreve no lugar; senão, exclui e anexa no fim
  update(id, produto) {
    const registro = this.localizar(id);
    if (!registro) {
      return false;
    }

    const dados = ArquivoProdutos.serializar(id, produto);

    if (dados.length <= registro.tamanho) {
      const espaco = new Int8Array(registro.tamanho);
      espaco.set(dados, 0);
      this.escrever(espaco, registro.inicioDados);
    } else {
      this.bytes[registro.inicio] = ArquivoProdutos.LAPIDE_EXCLUIDO;
      this.anexar(id, produto);
    }
    return true;
  }

  // Exclusão lógica: só marca a lápide
  delete(id) {
    const registro = this.localizar(id);
    if (!registro) {
      return false;
    }
    this.bytes[registro.inicio] = ArquivoProdutos.LAPIDE_EXCLUIDO;
    return true;
  }

  // Exclusão física: reconstrói o vetor sem os registros excluídos
  compactar() {
    const ativos = this.listarRegistros().filter((r) => r.ativo);
    const total = ativos.reduce((soma, r) => soma + r.fim - r.inicio, ArquivoProdutos.TAM_CABECALHO);
    const novo = new Int8Array(total);

    novo.set(this.bytes.subarray(0, ArquivoProdutos.TAM_CABECALHO), 0);
    let posicao = ArquivoProdutos.TAM_CABECALHO;
    for (const r of ativos) {
      novo.set(this.bytes.subarray(r.inicio, r.fim), posicao);
      posicao += r.fim - r.inicio;
    }

    const removidos = this.bytes.length - novo.length;
    this.bytes = novo;
    return removidos;
  }

  // Registro ativo com esse id, com sua posição no vetor
  localizar(id) {
    return this.listarRegistros().find((r) => r.ativo && r.produto.id === id) || null;
  }

  // Percorre o vetor e devolve a posição de cada registro e de cada campo
  listarRegistros() {
    const registros = [];
    let posicao = ArquivoProdutos.TAM_CABECALHO;

    while (posicao < this.bytes.length) {
      const lapide = this.bytes[posicao];
      const tamanho = ByteStreamProdutos.readShort(this.bytes, posicao + 1);
      const inicioDados = posicao + 3;
      const { produto, campos } = ArquivoProdutos.desserializar(this.bytes, inicioDados);

      registros.push({
        inicio: posicao,
        fim: inicioDados + tamanho,
        inicioDados,
        tamanho,
        ativo: lapide === ArquivoProdutos.LAPIDE_ATIVO,
        produto,
        campos,
      });
      posicao = inicioDados + tamanho;
    }
    return registros;
  }

  anexar(id, produto) {
    const dados = ArquivoProdutos.serializar(id, produto);
    const registro = new Int8Array(3 + dados.length);
    registro[0] = ArquivoProdutos.LAPIDE_ATIVO;
    registro.set(ByteStreamProdutos.writeShort(dados.length), 1);
    registro.set(dados, 3);

    const novo = new Int8Array(this.bytes.length + registro.length);
    novo.set(this.bytes, 0);
    novo.set(registro, this.bytes.length);
    this.bytes = novo;
  }

  escrever(trecho, posicao) {
    this.bytes.set(trecho, posicao);
  }

  static serializar(id, produto) {
    const partes = [
      ByteStreamProdutos.writeInt(id),
      ByteStreamProdutos.writeString(produto.nome ?? ""),
      ByteStreamProdutos.writeString(produto.descricao ?? ""),
      ByteStreamProdutos.writeFloat(Number(produto.preco) || 0),
      ByteStreamProdutos.writeInt(Number(produto.estoque) || 0),
      ByteStreamProdutos.writeDate(produto.dataCadastro ?? new Date()),
    ];

    const dados = new Int8Array(partes.reduce((soma, p) => soma + p.length, 0));
    let posicao = 0;
    for (const parte of partes) {
      dados.set(parte, posicao);
      posicao += parte.length;
    }
    return dados;
  }

  static desserializar(bytes, inicio) {
    const campos = [];
    let posicao = inicio;
    const ler = (nome, tamanho, valor) => {
      campos.push({ nome, inicio: posicao, tamanho });
      posicao += tamanho;
      return valor;
    };
    const tamanhoString = (p) => 2 + (ByteStreamProdutos.readShort(bytes, p) & 0xffff);

    const id = ler("id", 4, ByteStreamProdutos.readInt(bytes, posicao));
    const nome = ler("nome", tamanhoString(posicao), ByteStreamProdutos.readString(bytes, posicao));
    const descricao = ler("descricao", tamanhoString(posicao), ByteStreamProdutos.readString(bytes, posicao));
    const preco = ler("preco", 4, ByteStreamProdutos.readFloat(bytes, posicao));
    const estoque = ler("estoque", 4, ByteStreamProdutos.readInt(bytes, posicao));
    const data = ler("dataCadastro", 4, ByteStreamProdutos.readDate(bytes, posicao));
    const dataCadastro = data.toLocaleDateString("pt-BR", { timeZone: "UTC" });

    return { produto: { id, nome, descricao, preco, estoque, dataCadastro }, campos };
  }
}

if (typeof module !== "undefined" && module.exports) {
  module.exports = ArquivoProdutos;
}
