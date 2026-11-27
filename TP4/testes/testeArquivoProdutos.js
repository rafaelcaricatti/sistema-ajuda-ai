// Testes das quatro operações do CRUD no vetor de bytes.
// Rodar com: node TP4/testes/testeArquivoProdutos.js

const assert = require("node:assert");
const ByteStream = require("../js/ByteStream.js");
const ArquivoProdutos = require("../js/ArquivoProdutos.js");

const caneta = { nome: "Caneta", descricao: "Azul", preco: 2.5, estoque: 100, dataCadastro: "10/02/2025" };
const caderno = { nome: "Caderno", descricao: "200 folhas", preco: 25.9, estoque: 30, dataCadastro: "15/03/2025" };

function testes() {
  return {
    "inclusão grava cabeçalho e registro no vetor"() {
      const arq = new ArquivoProdutos();
      assert.strictEqual(arq.getBytes().length, 4);

      const id = arq.create(caneta);
      const bytes = arq.getBytes();

      assert.strictEqual(id, 1);
      assert.strictEqual(ByteStream.readInt(bytes, 0), 1, "último id no cabeçalho");
      assert.strictEqual(bytes[4], ArquivoProdutos.LAPIDE_ATIVO, "lápide ativa");
      assert.strictEqual(ByteStream.readShort(bytes, 5), bytes.length - 7, "tamanho do registro");
      assert.strictEqual(ByteStream.readInt(bytes, 7), 1, "id no início dos dados");
      assert.strictEqual(arq.create(caderno), 2);
    },

    "consulta lê os campos de volta dos bytes"() {
      const arq = new ArquivoProdutos();
      arq.create(caneta);
      const id = arq.create(caderno);

      const lido = arq.read(id);
      assert.strictEqual(lido.nome, "Caderno");
      assert.strictEqual(lido.descricao, "200 folhas");
      assert.ok(Math.abs(lido.preco - 25.9) < 0.001);
      assert.strictEqual(lido.estoque, 30);
      assert.strictEqual(lido.dataCadastro, "15/03/2025");
      assert.strictEqual(arq.read(99), null);
      assert.deepStrictEqual(arq.buscarPorNome("cad").map((p) => p.id), [2]);
    },

    "localiza registros e campos dentro do vetor"() {
      const arq = new ArquivoProdutos();
      arq.create(caneta);
      arq.create(caderno);

      const [r1, r2] = arq.listarRegistros();
      assert.strictEqual(r1.inicio, 4);
      assert.strictEqual(r2.inicio, r1.fim, "registros em sequência");
      assert.strictEqual(r2.fim, arq.getBytes().length);
      assert.deepStrictEqual(r1.campos.map((c) => c.nome), ["id", "nome", "descricao", "preco", "estoque", "dataCadastro"]);

      const nome = r1.campos[1];
      assert.strictEqual(ByteStream.readString(arq.getBytes(), nome.inicio), "Caneta");
      assert.strictEqual(nome.tamanho, 2 + "Caneta".length);
      assert.strictEqual(arq.localizar(2).inicio, r2.inicio);
    },

    "alteração menor reescreve no mesmo lugar"() {
      const arq = new ArquivoProdutos();
      arq.create(caneta);
      arq.create(caderno);
      const tamanhoAntes = arq.getBytes().length;
      const inicioAntes = arq.localizar(1).inicio;

      assert.ok(arq.update(1, { ...caneta, nome: "Lápis", estoque: 80 }));

      assert.strictEqual(arq.getBytes().length, tamanhoAntes, "vetor não cresce");
      assert.strictEqual(arq.localizar(1).inicio, inicioAntes, "registro no mesmo lugar");
      assert.strictEqual(arq.read(1).nome, "Lápis");
      assert.strictEqual(arq.read(1).estoque, 80);
      assert.strictEqual(arq.read(2).nome, "Caderno", "vizinho intacto");
    },

    "alteração maior exclui o antigo e anexa no fim"() {
      const arq = new ArquivoProdutos();
      arq.create(caneta);
      arq.create(caderno);
      const inicioAntigo = arq.localizar(1).inicio;

      assert.ok(arq.update(1, { ...caneta, descricao: "Azul, ponta fina, corpo transparente" }));

      const bytes = arq.getBytes();
      assert.strictEqual(bytes[inicioAntigo], ArquivoProdutos.LAPIDE_EXCLUIDO, "antigo com lápide");
      assert.ok(arq.localizar(1).inicio > arq.localizar(2).inicio, "novo no fim");
      assert.strictEqual(arq.read(1).descricao, "Azul, ponta fina, corpo transparente");
      assert.strictEqual(arq.readAll().length, 2);
      assert.strictEqual(arq.ultimoId(), 2, "alterar não gera id novo");
      assert.ok(!arq.update(99, caneta));
    },

    "exclusão lógica marca a lápide e some da consulta"() {
      const arq = new ArquivoProdutos();
      arq.create(caneta);
      arq.create(caderno);
      const inicio = arq.localizar(1).inicio;
      const tamanho = arq.getBytes().length;

      assert.ok(arq.delete(1));

      assert.strictEqual(arq.getBytes()[inicio], ArquivoProdutos.LAPIDE_EXCLUIDO);
      assert.strictEqual(arq.getBytes().length, tamanho, "bytes continuam no vetor");
      assert.strictEqual(arq.read(1), null);
      assert.ok(!arq.delete(1), "excluir de novo falha");
      assert.deepStrictEqual(arq.readAll().map((p) => p.id), [2]);
    },

    "exclusão física remove os bytes dos excluídos"() {
      const arq = new ArquivoProdutos();
      arq.create(caneta);
      arq.create(caderno);
      arq.delete(1);
      const tamanhoRegistro1 = arq.listarRegistros()[0].fim - 4;

      assert.strictEqual(arq.compactar(), tamanhoRegistro1);
      assert.strictEqual(arq.listarRegistros().length, 1);
      assert.strictEqual(arq.read(2).nome, "Caderno");
      assert.strictEqual(arq.create(caneta), 3, "ids continuam após compactar");
    },

    "reabre a partir dos bytes salvos"() {
      const arq = new ArquivoProdutos();
      arq.create(caneta);
      arq.create(caderno);
      arq.delete(1);

      const salvo = Array.from(arq.getBytes());
      const reaberto = new ArquivoProdutos(salvo);

      assert.strictEqual(reaberto.ultimoId(), 2);
      assert.strictEqual(reaberto.read(1), null);
      assert.strictEqual(reaberto.read(2).nome, "Caderno");
    },
  };
}

let falhas = 0;
for (const [nome, teste] of Object.entries(testes())) {
  try {
    teste();
    console.log("ok   - " + nome);
  } catch (erro) {
    falhas++;
    console.log("FALHA - " + nome + "\n       " + erro.message);
  }
}
console.log(falhas === 0 ? "\nTodos os testes passaram" : `\n${falhas} teste(s) falharam`);
process.exitCode = falhas === 0 ? 0 : 1;
