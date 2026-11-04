package br.com.malumashoes.venda.model.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converte o texto da coluna JSONB {@code itens_pedido} na lista de {@link ItemPedido}. Usa um
 * ObjectMapper próprio, sem as customizações do REST: os nomes das chaves vêm de
 * {@code @JsonProperty} em {@link ItemPedido}.
 */
final class ItensPedidoJson {

  private static final Logger log = LoggerFactory.getLogger(ItensPedidoJson.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final TypeReference<List<ItemPedido>> LISTA_DE_ITENS = new TypeReference<>() {};

  private ItensPedidoJson() {}

  /** Coluna nula ou vazia vira lista vazia; JSON quebrado é falha do dado gravado (500). */
  static List<ItemPedido> ler(final String json) {
    if (null == json || json.isBlank()) {
      return List.of();
    }
    try {
      return List.copyOf(MAPPER.readValue(json, LISTA_DE_ITENS));
    } catch (final JsonProcessingException e) {
      log.error("itens_pedido com JSON inválido em historico_vendas", e);
      throw new IllegalStateException("itens_pedido com JSON inválido", e);
    }
  }
}
