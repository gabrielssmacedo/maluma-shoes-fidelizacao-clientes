package br.com.malumashoes.venda.repository;

import br.com.malumashoes.venda.model.FiltroVendas;
import br.com.malumashoes.venda.model.entity.HistoricoVenda;
import br.com.malumashoes.venda.model.entity.HistoricoVenda_;
import br.com.malumashoes.venda.model.rest.response.VendaResumoResponse;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * Leitura de {@code historico_vendas}. Sempre da compra mais recente para a mais antiga, com o
 * {@code id} desempatando, para a ordem não mudar entre uma página e a próxima. Os filtros usam os
 * índices de {@code cliente_cpf} e {@code data_compra}.
 */
@ApplicationScoped
public class HistoricoVendaRepository implements PanacheRepositoryBase<HistoricoVenda, UUID> {

  static final Sort MAIS_RECENTES_PRIMEIRO =
      Sort.descending(HistoricoVenda_.DATA_COMPRA, HistoricoVenda_.ID);

  /** Só as colunas do resumo: a coluna JSONB dos itens não é lida. */
  public Uni<List<VendaResumoResponse>> listarResumos(final FiltroVendas filtro, final Page page) {
    return find(filtro.where(), MAIS_RECENTES_PRIMEIRO, filtro.parametros())
        .page(page)
        .project(VendaResumoResponse.class)
        .list();
  }

  public Uni<List<HistoricoVenda>> listar(final FiltroVendas filtro, final Page page) {
    return find(filtro.where(), MAIS_RECENTES_PRIMEIRO, filtro.parametros()).page(page).list();
  }

  public Uni<Long> contar(final FiltroVendas filtro) {
    return count(filtro.where(), filtro.parametros());
  }
}
