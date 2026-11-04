package br.com.malumashoes.venda.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItensPedidoJsonTest {

  @Test
  @DisplayName("lê as chaves gravadas pela ETL e ignora chave desconhecida")
  void leItens() {
    final List<ItemPedido> itens =
        ItensPedidoJson.ler(
            "[{\"sku_externo\": \"SKU-1\", \"tamanho\": 38, \"preco_unitario\": 99.9,"
                + " \"quantidade\": 2, \"novo\": true}]");
    assertEquals(1, itens.size());
    assertEquals("SKU-1", itens.getFirst().skuExterno());
    assertEquals(38, itens.getFirst().tamanho());
    assertEquals(new BigDecimal("99.9"), itens.getFirst().precoUnitario());
  }

  @Test
  @DisplayName("coluna nula ou vazia vira lista vazia")
  void vazio() {
    assertTrue(ItensPedidoJson.ler(null).isEmpty());
    assertTrue(ItensPedidoJson.ler(" ").isEmpty());
    assertTrue(ItensPedidoJson.ler("[]").isEmpty());
  }

  @Test
  @DisplayName("JSON quebrado é falha do dado gravado")
  void jsonInvalido() {
    assertThrows(IllegalStateException.class, () -> ItensPedidoJson.ler("[{"));
  }
}
