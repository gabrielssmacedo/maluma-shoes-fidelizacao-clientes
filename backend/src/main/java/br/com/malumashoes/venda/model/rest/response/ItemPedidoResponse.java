package br.com.malumashoes.venda.model.rest.response;

import java.math.BigDecimal;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Um item de um pedido: o calçado, o tamanho, a cor e quantos pares. */
@Schema(description = "Item de um pedido.")
public record ItemPedidoResponse(
    UUID id,
    @Schema(examples = "SKU-TNS-001") String skuExterno,
    @Schema(examples = "Tênis Runner Pro") String nomeProduto,
    @Schema(examples = "Tênis") String categoria,
    @Schema(examples = "40") Integer tamanho,
    @Schema(examples = "Preto") String cor,
    @Schema(examples = "199.90") BigDecimal precoUnitario,
    @Schema(examples = "1") Integer quantidade) {}
