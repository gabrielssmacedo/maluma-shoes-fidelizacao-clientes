package br.com.malumashoes.venda.model.rest.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Uma venda do histórico do cliente, com os itens do pedido. */
@Schema(description = "Venda com os itens do pedido.")
public record VendaDetalheResponse(
    UUID id,
    @Schema(description = "Id do pedido no Sistema de Vendas.") String pedidoExternoId,
    @Schema(description = "CPF do cliente, só os 11 dígitos.", examples = "12345678901")
        String clienteCpf,
    @Schema(description = "Data e hora da compra (ISO 8601).") OffsetDateTime dataCompra,
    @Schema(examples = "349.90") BigDecimal valorTotal,
    @Schema(examples = "PIX", nullable = true) String metodoPagamento,
    @Schema(description = "Itens do pedido.") List<ItemPedidoResponse> itensPedido) {

  public VendaDetalheResponse {
    itensPedido = List.copyOf(itensPedido);
  }
}
