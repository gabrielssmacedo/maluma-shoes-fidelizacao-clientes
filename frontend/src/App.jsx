import { useEffect, useState } from 'react';
import { buscarHistoricoCliente, buscarVendas } from './services/vendas.js';

const TAMANHO_PAGINA = 20;

function formatarCpf(cpf) {
  const numeros = String(cpf ?? '').replace(/\D/g, '');
  if (numeros.length !== 11) return cpf || '';
  return numeros.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
}

function formatarData(data) {
  if (!data) return 'Não informada';
  const dataConvertida = new Date(data);
  if (Number.isNaN(dataConvertida.getTime())) return 'Data inválida';

  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: 'America/Sao_Paulo',
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(dataConvertida);
}

function formatarValor(valor) {
  const numero = Number(valor);
  if (!Number.isFinite(numero)) return '—';
  return numero.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  });
}

function App() {
  const [vendas, setVendas] = useState([]);
  const [cpfBusca, setCpfBusca] = useState('');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [filtrosAplicados, setFiltrosAplicados] = useState({
    cpf: '',
    dataInicio: '',
    dataFim: '',
  });
  const [pagina, setPagina] = useState(0);
  const [totalVendas, setTotalVendas] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState('');
  const [atualizacao, setAtualizacao] = useState(0);

  const [clienteSelecionado, setClienteSelecionado] = useState('');
  const [historicoCliente, setHistoricoCliente] = useState(null);
  const [paginaCliente, setPaginaCliente] = useState(0);
  const [carregandoCliente, setCarregandoCliente] = useState(false);
  const [erroCliente, setErroCliente] = useState('');

  useEffect(() => {
    let cancelado = false;

    async function carregarVendas() {
      setCarregando(true);
      setErro('');

      try {
        const resultado = await buscarVendas({
          page: pagina,
          size: TAMANHO_PAGINA,
          cpf: filtrosAplicados.cpf,
          dataInicio: filtrosAplicados.dataInicio,
          dataFim: filtrosAplicados.dataFim,
        });

        if (cancelado) return;
        setVendas(resultado.content);
        setTotalVendas(resultado.totalElements);
        setTotalPaginas(resultado.totalPages);
      } catch (erroConsulta) {
        if (cancelado) return;
        setVendas([]);
        setTotalVendas(0);
        setTotalPaginas(0);
        setErro(erroConsulta.message || 'Não foi possível carregar as vendas.');
      } finally {
        if (!cancelado) setCarregando(false);
      }
    }

    carregarVendas();
    return () => {
      cancelado = true;
    };
  }, [pagina, filtrosAplicados, atualizacao]);

  useEffect(() => {
    if (!clienteSelecionado) return undefined;

    let cancelado = false;

    async function carregarHistoricoCliente() {
      setCarregandoCliente(true);
      setErroCliente('');
      setHistoricoCliente(null);

      try {
        const resultado = await buscarHistoricoCliente({
          cpf: clienteSelecionado,
          page: paginaCliente,
          size: TAMANHO_PAGINA,
        });

        if (!cancelado) setHistoricoCliente(resultado);
      } catch (erroConsulta) {
        if (!cancelado) {
          setErroCliente(erroConsulta.message || 'Não foi possível carregar o histórico do cliente.');
        }
      } finally {
        if (!cancelado) setCarregandoCliente(false);
      }
    }

    carregarHistoricoCliente();
    return () => {
      cancelado = true;
    };
  }, [clienteSelecionado, paginaCliente]);

  function pesquisarVendas(evento) {
    evento.preventDefault();
    const cpfNumeros = cpfBusca.replace(/\D/g, '');

    if (cpfNumeros !== '' && cpfNumeros.length !== 11) {
      setErro('Digite um CPF completo com 11 números ou deixe o campo vazio.');
      return;
    }

    if (dataInicio && dataFim && dataInicio > dataFim) {
      setErro('A data inicial não pode ser posterior à data final.');
      return;
    }

    setErro('');
    setPagina(0);
    setFiltrosAplicados({
      cpf: cpfNumeros,
      dataInicio,
      dataFim,
    });
    setAtualizacao((valorAtual) => valorAtual + 1);
  }

  function limparFiltros() {
    setCpfBusca('');
    setDataInicio('');
    setDataFim('');
    setErro('');
    setPagina(0);
    setFiltrosAplicados({ cpf: '', dataInicio: '', dataFim: '' });
    setAtualizacao((valorAtual) => valorAtual + 1);
  }

  function abrirHistoricoCliente(cpf) {
    setPaginaCliente(0);
    setHistoricoCliente(null);
    setErroCliente('');
    setClienteSelecionado(cpf);
  }

  function fecharHistoricoCliente() {
    setClienteSelecionado('');
    setHistoricoCliente(null);
    setErroCliente('');
    setPaginaCliente(0);
  }

  const primeiraVenda = totalVendas === 0 ? 0 : pagina * TAMANHO_PAGINA + 1;
  const ultimaVenda = Math.min((pagina + 1) * TAMANHO_PAGINA, totalVendas);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">M</div>
          <div>
            <strong>MALUMA</strong>
            <span>SHOES</span>
          </div>
        </div>

        <div className="sidebar-section-label">MENU PRINCIPAL</div>
        <nav className="side-navigation" aria-label="Menu principal">
          <div className="nav-item nav-item-active">
            <span className="nav-icon" aria-hidden="true">▤</span>
            <span>Histórico de vendas</span>
          </div>
        </nav>

        <div className="sidebar-footer">
          <span className="status-indicator" />
          Sistema de fidelização
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <div className="breadcrumb">Maluma Shoes <span>/</span> Vendas</div>
            <h1>Histórico de vendas</h1>
          </div>
          <div className="topbar-label">Painel administrativo</div>
        </header>

        <section className="page-content">
          <div className="page-intro">
            <div>
              <h2>Consulta de vendas</h2>
              <p>Consulte as compras realizadas e o histórico dos clientes.</p>
            </div>
            <div className="total-card">
              <span>Vendas encontradas</span>
              <strong>{totalVendas.toLocaleString('pt-BR')}</strong>
            </div>
          </div>

          <section className="filter-card" aria-labelledby="filtros-titulo">
            <div className="section-heading">
              <div>
                <h3 id="filtros-titulo">Filtros de pesquisa</h3>
                <p>Informe um CPF, um período ou combine os filtros.</p>
              </div>
            </div>

            <form className="filters-form" onSubmit={pesquisarVendas}>
              <label className="form-field cpf-field">
                <span>CPF do cliente</span>
                <input
                  type="text"
                  inputMode="numeric"
                  autoComplete="off"
                  maxLength={14}
                  placeholder="000.000.000-00"
                  value={cpfBusca}
                  onChange={(evento) => setCpfBusca(evento.target.value)}
                />
              </label>

              <label className="form-field">
                <span>Data inicial</span>
                <input
                  type="date"
                  value={dataInicio}
                  onChange={(evento) => setDataInicio(evento.target.value)}
                />
              </label>

              <label className="form-field">
                <span>Data final</span>
                <input
                  type="date"
                  value={dataFim}
                  onChange={(evento) => setDataFim(evento.target.value)}
                />
              </label>

              <div className="filter-actions">
                <button className="button button-secondary" type="button" onClick={limparFiltros}>
                  Limpar
                </button>
                <button className="button button-primary" type="submit">
                  Pesquisar
                </button>
              </div>
            </form>
            {erro && <div className="message message-error" role="alert">{erro}</div>}
          </section>

          <section className="results-card" aria-labelledby="resultados-titulo">
            <div className="results-heading">
              <div>
                <h3 id="resultados-titulo">Vendas registradas</h3>
                <p>Exibindo os resultados mais recentes primeiro.</p>
              </div>
              <span className="page-size-label">{TAMANHO_PAGINA} por página</span>
            </div>

            <div className="table-wrapper">
              <table className="sales-table">
                <thead>
                  <tr>
                    <th>Pedido</th>
                    <th>CPF do cliente</th>
                    <th>Data da compra</th>
                    <th>Valor total</th>
                    <th>Método de pagamento</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {carregando ? (
                    <tr><td className="table-message" colSpan="6">Carregando vendas...</td></tr>
                  ) : vendas.length > 0 ? (
                    vendas.map((venda) => (
                      <tr key={venda.id}>
                        <td className="order-id">{venda.pedidoExternoId || venda.id}</td>
                        <td>{formatarCpf(venda.clienteCpf)}</td>
                        <td>{formatarData(venda.dataCompra)}</td>
                        <td className="money-cell">{formatarValor(venda.valorTotal)}</td>
                        <td>{venda.metodoPagamento || 'Não informado'}</td>
                        <td>
                          <button
                            className="text-button"
                            type="button"
                            onClick={() => abrirHistoricoCliente(venda.clienteCpf)}
                          >
                            Histórico do cliente
                          </button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td className="table-message" colSpan="6">
                        {erro ? 'Não foi possível carregar os resultados.' : 'Nenhuma venda encontrada.'}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            <div className="pagination-bar">
              <span>
                {totalVendas > 0
                  ? `Exibindo ${primeiraVenda}–${ultimaVenda} de ${totalVendas.toLocaleString('pt-BR')} vendas`
                  : 'Nenhuma venda para exibir'}
              </span>
              <div className="pagination-controls">
                <button
                  className="button button-secondary pagination-button"
                  type="button"
                  disabled={pagina <= 0 || carregando}
                  onClick={() => setPagina((paginaAtual) => Math.max(0, paginaAtual - 1))}
                >
                  Anterior
                </button>
                <span className="pagination-current">
                  Página {totalPaginas === 0 ? 0 : pagina + 1} de {totalPaginas}
                </span>
                <button
                  className="button button-secondary pagination-button"
                  type="button"
                  disabled={totalPaginas === 0 || pagina >= totalPaginas - 1 || carregando}
                  onClick={() => setPagina((paginaAtual) => paginaAtual + 1)}
                >
                  Próxima
                </button>
              </div>
            </div>
          </section>
        </section>
      </main>

      {clienteSelecionado && (
        <div className="modal-backdrop" role="presentation" onMouseDown={(evento) => {
          if (evento.target === evento.currentTarget) fecharHistoricoCliente();
        }}>
          <section className="history-modal" role="dialog" aria-modal="true" aria-labelledby="historico-cliente-titulo">
            <header className="modal-header">
              <div>
                <span className="modal-eyebrow">Histórico individual</span>
                <h2 id="historico-cliente-titulo">Compras do cliente</h2>
                <p>CPF: {formatarCpf(clienteSelecionado)}</p>
              </div>
              <button className="close-button" type="button" onClick={fecharHistoricoCliente} aria-label="Fechar histórico">
                ×
              </button>
            </header>

            <div className="modal-content">
              {carregandoCliente && <div className="modal-state">Carregando histórico do cliente...</div>}
              {erroCliente && <div className="message message-error" role="alert">{erroCliente}</div>}

              {!carregandoCliente && !erroCliente && historicoCliente && (
                <>
                  {historicoCliente.content.length === 0 ? (
                    <div className="modal-state">Esse cliente não possui compras registradas.</div>
                  ) : (
                    <div className="customer-orders">
                      {historicoCliente.content.map((venda) => (
                        <article className="order-card" key={venda.id}>
                          <div className="order-card-header">
                            <div>
                              <strong>{venda.pedidoExternoId || venda.id}</strong>
                              <span>{formatarData(venda.dataCompra)}</span>
                            </div>
                            <div className="order-total">
                              <strong>{formatarValor(venda.valorTotal)}</strong>
                              <span>{venda.metodoPagamento || 'Pagamento não informado'}</span>
                            </div>
                          </div>

                          <div className="items-list">
                            <h3>Itens do pedido</h3>
                            {Array.isArray(venda.itensPedido) && venda.itensPedido.length > 0 ? (
                              venda.itensPedido.map((item) => (
                                <div className="item-row" key={item.id}>
                                  <div>
                                    <strong>{item.nomeProduto || 'Produto sem nome'}</strong>
                                    <span>
                                      {[item.categoria, item.cor, item.tamanho ? `Tam. ${item.tamanho}` : '']
                                        .filter(Boolean)
                                        .join(' · ')}
                                    </span>
                                    <small>{item.skuExterno || 'SKU não informado'}</small>
                                  </div>
                                  <div className="item-price">
                                    <strong>{formatarValor(item.precoUnitario)}</strong>
                                    <span>Qtd.: {item.quantidade ?? 0}</span>
                                  </div>
                                </div>
                              ))
                            ) : (
                              <p className="no-items">A API não retornou itens para este pedido.</p>
                            )}
                          </div>
                        </article>
                      ))}
                    </div>
                  )}

                  <div className="pagination-bar modal-pagination">
                    <span>{historicoCliente.totalElements.toLocaleString('pt-BR')} compra(s) no total</span>
                    <div className="pagination-controls">
                      <button
                        className="button button-secondary pagination-button"
                        type="button"
                        disabled={paginaCliente <= 0 || carregandoCliente}
                        onClick={() => setPaginaCliente((paginaAtual) => Math.max(0, paginaAtual - 1))}
                      >
                        Anterior
                      </button>
                      <span className="pagination-current">
                        Página {historicoCliente.totalPages === 0 ? 0 : paginaCliente + 1} de {historicoCliente.totalPages}
                      </span>
                      <button
                        className="button button-secondary pagination-button"
                        type="button"
                        disabled={historicoCliente.totalPages === 0 || paginaCliente >= historicoCliente.totalPages - 1 || carregandoCliente}
                        onClick={() => setPaginaCliente((paginaAtual) => paginaAtual + 1)}
                      >
                        Próxima
                      </button>
                    </div>
                  </div>
                </>
              )}
            </div>
          </section>
        </div>
      )}
    </div>
  );
}

export default App;
