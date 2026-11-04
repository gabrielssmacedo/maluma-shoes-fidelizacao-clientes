package br.com.malumashoes.infrastructure.model.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.malumashoes.infrastructure.model.rest.erro.CodigoErro;
import br.com.malumashoes.infrastructure.model.rest.pagina.PaginaParams;
import br.com.malumashoes.infrastructure.model.rest.pagina.PaginaResponse;
import io.quarkiverse.httpproblem.HttpProblem;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PaginaECodigoErroTest {

  @Test
  @DisplayName("page ausente vira 0; o informado é usado")
  void page() {
    assertEquals(0, PaginaParams.of(null, null).index);
    assertEquals(0, PaginaParams.of(" ", null).index);
    assertEquals(3, PaginaParams.of(" 3 ", null).index);
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1", "abc", "1.5", "99999999999"})
  @DisplayName("page negativo, não numérico ou fora do int é recusado com PAGINA_INVALIDA")
  void pageInvalido(final String page) {
    final HttpProblem erro = assertThrows(HttpProblem.class, () -> PaginaParams.of(page, null));
    assertEquals("PAGINA_INVALIDA", erro.getParameters().get("code"));
  }

  @Test
  @DisplayName("page cujo deslocamento (page * size) estouraria o int é recusado")
  void pageQueEstouraODeslocamento() {
    final int maior = Integer.MAX_VALUE / 100;
    assertEquals(maior, PaginaParams.of(String.valueOf(maior), "100").index);
    assertThrows(HttpProblem.class, () -> PaginaParams.of(String.valueOf(maior + 1), "100"));
  }

  @Test
  @DisplayName("size ausente ou < 1 vira 20; acima de 100 vira 100; não numérico é recusado")
  void size() {
    assertEquals(20, PaginaParams.of(null, null).size);
    assertEquals(20, PaginaParams.of(null, "-5").size);
    assertEquals(7, PaginaParams.of(null, "7").size);
    assertEquals(100, PaginaParams.of(null, "99999999999").size);
    assertThrows(HttpProblem.class, () -> PaginaParams.of(null, "dez"));
  }

  @Test
  @DisplayName("totalPages arredonda para cima e é 0 sem itens")
  void totalPages() {
    assertEquals(3, PaginaResponse.of(List.of(1), 0, 2, 5, Function.identity()).totalPages());
    assertEquals(2, PaginaResponse.of(List.of(1), 0, 2, 4, Function.identity()).totalPages());
    assertEquals(0, PaginaResponse.of(List.of(), 0, 2, 0, Function.identity()).totalPages());
  }

  @Test
  @DisplayName("código genérico pelo status: 404, outros 4xx e 5xx")
  void codigoPeloStatus() {
    assertEquals(CodigoErro.NAO_ENCONTRADO, CodigoErro.doStatus(404));
    assertEquals(CodigoErro.NAO_AUTENTICADO, CodigoErro.doStatus(401));
    assertEquals(CodigoErro.REQUISICAO_INVALIDA, CodigoErro.doStatus(405));
    assertEquals(CodigoErro.ERRO_INTERNO, CodigoErro.doStatus(500));
    for (final CodigoErro codigo : CodigoErro.values()) {
      assertFalse(codigo.descricao().isBlank(), codigo.name());
    }
  }
}
