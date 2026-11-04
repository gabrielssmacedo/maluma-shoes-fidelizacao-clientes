package br.com.malumashoes.venda.model.rest.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Uma linha da tabela principal de vendas, sem os itens do pedido. Também é a projeção da consulta
 * ({@code project(VendaResumoResponse.class)}): os nomes dos componentes precisam bater com os
 * atributos de {@code HistoricoVenda}, e a coluna JSONB nem é lida.
 */
@Schema(description = "Venda resumida, sem os itens do pedido.")
public record VendaResumoResponse(
    UUID id,
    @Schema(description = "Id do pedido no Sistema de Vendas.") String pedidoExternoId,
    @Schema(description = "CPF do cliente, só os 11 dígitos.", examples = "12345678901")
        String clienteCpf,
    @Schema(description = "Data e hora da compra (ISO 8601).") OffsetDateTime dataCompra,
    @Schema(examples = "349.90") BigDecimal valorTotal,
    @Schema(examples = "PIX", nullable = true) String metodoPagamento) {}
