package br.com.malumashoes.venda.service;

import br.com.malumashoes.infrastructure.model.Cpf;
import br.com.malumashoes.infrastructure.model.rest.pagina.PaginaParams;
import br.com.malumashoes.infrastructure.model.rest.pagina.PaginaResponse;
import br.com.malumashoes.venda.model.FiltroVendas;
import br.com.malumashoes.venda.model.entity.HistoricoVenda;
import br.com.malumashoes.venda.model.rest.response.VendaDetalheResponse;
import br.com.malumashoes.venda.model.rest.response.VendaResumoResponse;
import br.com.malumashoes.venda.repository.HistoricoVendaRepository;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Histórico de vendas do CRM: a listagem geral (tabela principal) e o histórico de um cliente.
 *
 * <p>A contagem e a página rodam uma depois da outra, nunca em paralelo: o Hibernate Reactive não
 * aceita duas operações ao mesmo tempo na mesma sessão.
 */
@ApplicationScoped
public class VendaService {

  private final HistoricoVendaRepository repository;
  private final ZoneId zona;

  @Inject
  public VendaService(
      final HistoricoVendaRepository repository,
      @ConfigProperty(name = "app.timezone") final ZoneId zona) {
    this.repository = repository;
    this.zona = zona;
  }

  /** Listagem geral, mais recentes primeiro, com filtros opcionais de CPF e período. */
  public Uni<PaginaResponse<VendaResumoResponse>> listar(
      final String page,
      final String size,
      final String cpf,
      final String dataInicio,
      final String dataFim) {
    final FiltroVendas filtro = FiltroVendas.of(cpf, dataInicio, dataFim, zona);
    final Page pagina = PaginaParams.of(page, size);
    return repository
        .contar(filtro)
        .flatMap(
            total ->
                repository
                    .listarResumos(filtro, pagina)
                    .map(itens -> montar(itens, pagina, total, Function.identity())));
  }

  /** Todo o histórico de um cliente, mais recentes primeiro, com os itens de cada pedido. */
  public Uni<PaginaResponse<VendaDetalheResponse>> listarDoCliente(
      final String cpf, final String page, final String size) {
    final FiltroVendas filtro = FiltroVendas.doCliente(Cpf.of(cpf));
    final Page pagina = PaginaParams.of(page, size);
    return repository
        .contar(filtro)
        .flatMap(
            total ->
                repository
                    .listar(filtro, pagina)
                    .map(itens -> montar(itens, pagina, total, HistoricoVenda::toDetalheResponse)));
  }

  private static <E, T> PaginaResponse<T> montar(
      final List<E> itens, final Page pagina, final long total, final Function<E, T> conversor) {
    return PaginaResponse.of(itens, pagina.index, pagina.size, total, conversor);
  }
}
