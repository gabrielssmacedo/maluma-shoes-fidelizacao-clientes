package br.com.malumashoes.venda.model.entity;

import br.com.malumashoes.venda.model.rest.response.ItemPedidoResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Um item do array JSONB {@code historico_vendas.itens_pedido}, com os nomes de chave gravados pela
 * carga inicial ({@code postgres-init/03-...}) e pela ETL do Airflow. Chave a mais é ignorada, para
 * o Sistema de Vendas poder acrescentar campos sem derrubar a leitura.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemPedido(
    @JsonProperty("id") UUID id,
    @JsonProperty("compra_id") UUID compraId,
    @JsonProperty("sku_externo") String skuExterno,
    @JsonProperty("nome_produto") String nomeProduto,
    @JsonProperty("categoria") String categoria,
    @JsonProperty("tamanho") Integer tamanho,
    @JsonProperty("cor") String cor,
    @JsonProperty("preco_unitario") BigDecimal precoUnitario,
    @JsonProperty("quantidade") Integer quantidade) {

  public ItemPedidoResponse toResponse() {
    return new ItemPedidoResponse(
        id, skuExterno, nomeProduto, categoria, tamanho, cor, precoUnitario, quantidade);
  }
}
