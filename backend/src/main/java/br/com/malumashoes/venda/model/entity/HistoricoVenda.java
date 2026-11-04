package br.com.malumashoes.venda.model.entity;

import br.com.malumashoes.venda.model.rest.response.ItemPedidoResponse;
import br.com.malumashoes.venda.model.rest.response.VendaDetalheResponse;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Immutable;

/**
 * Uma venda do Sistema de Vendas, copiada para {@code historico_vendas} pela carga inicial e pela
 * ETL do Airflow. Somente leitura para a API ({@link Immutable}): quem grava é a ETL, e os itens
 * ficam numa coluna JSONB só.
 */
@Entity
@Immutable
@Table(name = "historico_vendas")
public class HistoricoVenda extends PanacheEntityBase {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "pedido_externo_id", nullable = false)
  private String pedidoExternoId;

  @Column(name = "cliente_cpf", nullable = false)
  private String clienteCpf;

  @Column(name = "data_compra", nullable = false)
  private OffsetDateTime dataCompra;

  @Column(name = "valor_total", nullable = false)
  private BigDecimal valorTotal;

  @Column(name = "metodo_pagamento")
  private String metodoPagamento;

  /**
   * O array JSONB lido como texto e convertido por {@link ItensPedidoJson}: o cliente reativo do
   * Postgres só converte JSONB cujo topo é um objeto, e aqui o topo é um array (dá
   * ClassCastException de JsonArray para JsonObject com {@code @JdbcTypeCode(SqlTypes.JSON)}).
   */
  @ColumnTransformer(read = "itens_pedido::text")
  @Column(name = "itens_pedido", nullable = false, columnDefinition = "jsonb")
  private String itensPedido;

  protected HistoricoVenda() {}

  public VendaDetalheResponse toDetalheResponse() {
    final List<ItemPedidoResponse> itens =
        ItensPedidoJson.ler(itensPedido).stream().map(ItemPedido::toResponse).toList();
    return new VendaDetalheResponse(
        id, pedidoExternoId, clienteCpf, dataCompra, valorTotal, metodoPagamento, itens);
  }
}
