package br.com.malumashoes.venda;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import br.com.malumashoes.infrastructure.model.rest.erro.CodigoErro;
import br.com.malumashoes.infrastructure.security.ApiKeyFilter;
import br.com.malumashoes.support.ApiTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** GET /api/v1/vendas sobre a massa fixa de src/test/resources/db/massa-de-teste.sql. */
@QuarkusTest
class VendaResourceTest extends ApiTest {

  private static final String VENDAS = "/api/v1/vendas";
  private static final String PEDIDOS = "content.pedidoExternoId";
  private static final String TOTAL = "totalElements";
  private static final String CODE = "code";

  @Test
  @DisplayName("sem filtros lista tudo, da compra mais recente para a mais antiga")
  void listaTudoMaisRecentesPrimeiro() {
    given()
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(TOTAL, equalTo(8))
        .body("totalPages", equalTo(1))
        .body("page", equalTo(0))
        .body("size", equalTo(20))
        .body(
            PEDIDOS,
            contains(
                "pedido-a5",
                "pedido-a4",
                "pedido-c1",
                "pedido-b1",
                "pedido-a3",
                "pedido-a2",
                "pedido-a1",
                "pedido-b2"));
  }

  @Test
  @DisplayName("o resumo traz os campos da venda e não traz os itens do pedido")
  void resumoSemItens() {
    given()
        .queryParam("size", 1)
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body("content[0].id", equalTo("00000000-0000-0000-0000-0000000000a5"))
        .body("content[0].clienteCpf", equalTo("11111111111"))
        .body("content[0].valorTotal", equalTo(500.0f))
        .body("content[0].metodoPagamento", equalTo("CARTAO_DEBITO"))
        .body("content[0].dataCompra", equalTo("2026-03-20T15:00:00Z"))
        .body("content[0]", not(hasKey("itensPedido")));
  }

  @Test
  @DisplayName("pagina pela mesma ordem: a segunda página de 3 começa na quarta venda")
  void paginacao() {
    given()
        .queryParam("page", 1)
        .queryParam("size", 3)
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(TOTAL, equalTo(8))
        .body("totalPages", equalTo(3))
        .body("page", equalTo(1))
        .body(PEDIDOS, contains("pedido-b1", "pedido-a3", "pedido-a2"));
  }

  @Test
  @DisplayName("size acima do máximo vira 100; size zero vira o padrão 20")
  void sizeNormalizado() {
    given().queryParam("size", 500).when().get(VENDAS).then().body("size", equalTo(100));
    given().queryParam("size", 0).when().get(VENDAS).then().body("size", equalTo(20));
  }

  @Test
  @DisplayName("filtra pelo CPF, aceitando a pontuação")
  void filtraPorCpf() {
    given()
        .queryParam("cpf", "222.222.222-22")
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(TOTAL, equalTo(2))
        .body(PEDIDOS, contains("pedido-b1", "pedido-b2"));
  }

  @Test
  @DisplayName("o período vale no fuso de São Paulo e a data_fim entra inteira")
  void filtraPorPeriodoNoFusoDaLoja() {
    given()
        .queryParam("data_inicio", "2026-03-10")
        .queryParam("data_fim", "2026-03-10")
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body(PEDIDOS, contains("pedido-c1", "pedido-b1", "pedido-a3"));
  }

  @Test
  @DisplayName("só a data_fim: tudo até o fim daquele dia")
  void filtraSoPelaDataFim() {
    given()
        .queryParam("data_fim", "2026-03-09")
        .when()
        .get(VENDAS)
        .then()
        .body(PEDIDOS, contains("pedido-a2", "pedido-a1", "pedido-b2"));
  }

  @Test
  @DisplayName("CPF e período juntos")
  void filtraPorCpfEPeriodo() {
    given()
        .queryParam("cpf", "11111111111")
        .queryParam("data_inicio", "2026-03-10")
        .when()
        .get(VENDAS)
        .then()
        .body(PEDIDOS, contains("pedido-a5", "pedido-a4", "pedido-a3"));
  }

  @Test
  @DisplayName("CPF inválido é recusado com 400 e code CPF_INVALIDO")
  void cpfInvalido() {
    given()
        .queryParam("cpf", "123")
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(CODE, equalTo(CodigoErro.CPF_INVALIDO.name()));
  }

  @Test
  @DisplayName("data fora do formato é recusada com 400 e code DATA_INVALIDA")
  void dataInvalida() {
    given()
        .queryParam("data_inicio", "10/03/2026")
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(CODE, equalTo(CodigoErro.DATA_INVALIDA.name()))
        .body("detail", equalTo("data_inicio inválida: use o formato yyyy-MM-dd."));
  }

  @Test
  @DisplayName("data_inicio depois da data_fim é recusada com 400 e code PERIODO_INVALIDO")
  void periodoInvertido() {
    given()
        .queryParam("data_inicio", "2026-03-11")
        .queryParam("data_fim", "2026-03-10")
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(CODE, equalTo(CodigoErro.PERIODO_INVALIDO.name()));
  }

  @Test
  @DisplayName("página negativa é recusada com 400 e code PAGINA_INVALIDA")
  void paginaNegativa() {
    given()
        .queryParam("page", -1)
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(CODE, equalTo(CodigoErro.PAGINA_INVALIDA.name()));
  }

  @Test
  @DisplayName(
      "page não numérico é 400 (não 404) e page que estoura o deslocamento é 400 (não 500)")
  void pageMalformadoOuGigante() {
    given()
        .queryParam("page", "abc")
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(CODE, equalTo(CodigoErro.PAGINA_INVALIDA.name()));
    given()
        .queryParam("page", Integer.MAX_VALUE)
        .queryParam("size", 100)
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(CODE, equalTo(CodigoErro.PAGINA_INVALIDA.name()));
  }

  @Test
  @DisplayName("sem o header X-API-Key a API recusa com 401 e code NAO_AUTENTICADO")
  void semChaveDeApi() {
    semChave();
    given()
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.UNAUTHORIZED.getStatusCode())
        .body(CODE, equalTo(CodigoErro.NAO_AUTENTICADO.name()));
    given()
        .when()
        .get("/api/v1/clientes/{cpf}/vendas", "11111111111")
        .then()
        .statusCode(Status.UNAUTHORIZED.getStatusCode());
  }

  @Test
  @DisplayName("chave de API errada é recusada com 401")
  void chaveErrada() {
    semChave();
    given()
        .header(ApiKeyFilter.HEADER, "chave-errada")
        .when()
        .get(VENDAS)
        .then()
        .statusCode(Status.UNAUTHORIZED.getStatusCode())
        .body(CODE, equalTo(CodigoErro.NAO_AUTENTICADO.name()));
  }

  @Test
  @DisplayName("o health check não exige chave (é usado pelo healthcheck do Docker)")
  void healthSemChave() {
    semChave();
    given().when().get("/q/health/ready").then().statusCode(Status.OK.getStatusCode());
  }

  @Test
  @DisplayName("rota inexistente devolve 404 com o code genérico NAO_ENCONTRADO")
  void rotaInexistente() {
    given()
        .when()
        .get("/api/v1/nao-existe")
        .then()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .body(CODE, equalTo(CodigoErro.NAO_ENCONTRADO.name()));
  }
}
