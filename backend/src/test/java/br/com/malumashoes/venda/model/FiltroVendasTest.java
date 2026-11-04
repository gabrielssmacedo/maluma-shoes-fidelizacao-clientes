package br.com.malumashoes.venda.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkiverse.httpproblem.HttpProblem;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FiltroVendasTest {

  private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

  @Test
  @DisplayName("sem filtros: cláusula e parâmetros vazios")
  void semFiltros() {
    final FiltroVendas filtro = FiltroVendas.of(null, null, " ", SAO_PAULO);
    assertEquals("", filtro.where());
    assertTrue(filtro.parametros().map().isEmpty());
  }

  @Test
  @DisplayName("data_inicio vira o começo do dia e data_fim o começo do dia seguinte, em SP")
  void periodoNoFusoDaLoja() {
    final FiltroVendas filtro =
        FiltroVendas.of("11111111111", "2026-03-10", "2026-03-12", SAO_PAULO);
    assertEquals(OffsetDateTime.parse("2026-03-10T00:00:00-03:00"), filtro.inicio());
    assertEquals(OffsetDateTime.parse("2026-03-13T00:00:00-03:00"), filtro.fimExclusivo());
    assertEquals(
        "clienteCpf = :cpf and dataCompra >= :inicio and dataCompra < :fim", filtro.where());
    assertEquals("11111111111", filtro.parametros().map().get("cpf"));
  }

  @Test
  @DisplayName("o mesmo dia em data_inicio e data_fim é um período válido")
  void mesmoDia() {
    final FiltroVendas filtro = FiltroVendas.of(null, "2026-03-10", "2026-03-10", SAO_PAULO);
    assertNull(filtro.cpf());
    assertEquals("dataCompra >= :inicio and dataCompra < :fim", filtro.where());
  }

  @Test
  @DisplayName("data fora do formato é recusada com DATA_INVALIDA citando o parâmetro")
  void dataInvalida() {
    final HttpProblem erro =
        assertThrows(HttpProblem.class, () -> FiltroVendas.of(null, null, "2026-13-01", SAO_PAULO));
    assertEquals("DATA_INVALIDA", erro.getParameters().get("code"));
    assertEquals("data_fim inválida: use o formato yyyy-MM-dd.", erro.getDetail());
  }

  @Test
  @DisplayName("período invertido é recusado com PERIODO_INVALIDO")
  void periodoInvertido() {
    final HttpProblem erro =
        assertThrows(
            HttpProblem.class, () -> FiltroVendas.of(null, "2026-03-11", "2026-03-10", SAO_PAULO));
    assertEquals("PERIODO_INVALIDO", erro.getParameters().get("code"));
  }
}
