const API_BASE = '/api/v1';

const MENSAGENS_ERRO = {
  NAO_AUTENTICADO: 'A API recusou a autenticação. Confira a variável API_KEY do arquivo .env.',
  CPF_INVALIDO: 'O CPF informado precisa conter exatamente 11 números.',
  DATA_INVALIDA: 'Uma das datas está inválida. Use o formato de data do calendário.',
  PERIODO_INVALIDO: 'A data inicial não pode ser posterior à data final.',
  PAGINA_INVALIDA: 'A página solicitada é inválida. Atualize a tela e tente novamente.',
  REQUISICAO_INVALIDA: 'A API não aceitou os parâmetros enviados.',
  NAO_ENCONTRADO: 'O endereço solicitado não foi encontrado na API.',
  ERRO_INTERNO: 'O backend encontrou um erro interno. Consulte os logs do servidor.',
};

async function requisitar(caminho) {
  let resposta;

  try {
    resposta = await fetch(`${API_BASE}${caminho}`, {
      method: 'GET',
      headers: {
        Accept: 'application/json',
      },
    });
  } catch (erro) {
    const erroConexao = new Error(
      'Não foi possível conectar ao backend. Verifique se os serviços do Docker estão ativos.',
    );
    erroConexao.causa = erro;
    throw erroConexao;
  }

  const corpoTexto = await resposta.text();
  let corpo = null;

  if (corpoTexto) {
    try {
      corpo = JSON.parse(corpoTexto);
    } catch {
      corpo = null;
    }
  }

  if (!resposta.ok) {
    const codigo = corpo?.code;
    const mensagem = MENSAGENS_ERRO[codigo]
      || corpo?.detail
      || `A API respondeu com o status HTTP ${resposta.status}.`;
    const erroApi = new Error(mensagem);
    erroApi.status = resposta.status;
    erroApi.code = codigo;
    throw erroApi;
  }

  if (!corpo || !Array.isArray(corpo.content)) {
    throw new Error('A resposta do backend não está no formato de página esperado.');
  }

  return corpo;
}

/** Consulta a listagem geral de vendas, com os filtros e a paginação do backend. */
export function buscarVendas({
  page = 0,
  size = 20,
  cpf = '',
  dataInicio = '',
  dataFim = '',
} = {}) {
  const parametros = new URLSearchParams();
  parametros.set('page', String(page));
  parametros.set('size', String(size));

  const cpfNumeros = String(cpf).replace(/\D/g, '');
  if (cpfNumeros) parametros.set('cpf', cpfNumeros);
  if (dataInicio) parametros.set('data_inicio', dataInicio);
  if (dataFim) parametros.set('data_fim', dataFim);

  return requisitar(`/vendas?${parametros.toString()}`);
}

/** Consulta as compras de um cliente, incluindo os itens de cada pedido. */
export function buscarHistoricoCliente({ cpf, page = 0, size = 20 } = {}) {
  const cpfNumeros = String(cpf ?? '').replace(/\D/g, '');

  if (cpfNumeros.length !== 11) {
    return Promise.reject(new Error('Informe um CPF completo com 11 números.'));
  }

  const parametros = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  return requisitar(
    `/clientes/${encodeURIComponent(cpfNumeros)}/vendas?${parametros.toString()}`,
  );
}

export async function buscarVendaDoCliente({ cpf, vendaId }) {
  const cpfLimpo = cpf.replace(/\D/g, "");

  if (cpfLimpo.length !== 11) {
    throw new Error("CPF inválido.");
  }

  let page = 0;
  let totalPages = 1;

  while (page < totalPages) {
    const params = new URLSearchParams({
      page: String(page),
      size: "100",
    });

    const response = await fetch(
      `/api/v1/clientes/${cpfLimpo}/vendas?${params}`
    );

    if (!response.ok) {
      throw new Error("Não foi possível buscar os itens da venda.");
    }

    const resultado = await response.json();
    totalPages = resultado.totalPages;

    const vendaEncontrada = resultado.content.find(
      (venda) => venda.id === vendaId
    );

    if (vendaEncontrada) {
      return vendaEncontrada;
    }

    page++;
  }

  throw new Error("Venda não encontrada no histórico do cliente.");
}
