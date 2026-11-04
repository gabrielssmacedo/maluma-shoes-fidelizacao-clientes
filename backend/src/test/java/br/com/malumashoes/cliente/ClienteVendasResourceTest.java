package br.com.malumashoes.cliente;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import br.com.malumashoes.infrastructure.model.rest.erro.CodigoErro;
import br.com.malumashoes.support.ApiTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** GET /api/v1/clientes/{cpf}/vendas sobre a massa fixa de massa-de-teste.sql. */
@QuarkusTest
class ClienteVendasResourceTest extends ApiTest {

  private static final String VENDAS_DO_CLIENTE = "/api/v1/clientes/{cpf}/vendas";

  @Test
  @DisplayName("traz o histórico do cliente com os itens de cada pedido")
  void historicoComItens() {
    given()
        .when()
        .get(VENDAS_DO_CLIENTE, "333.333.333-33")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body("totalElements", equalTo(1))
        .body("content[0].pedidoExternoId", equalTo("pedido-c1"))
        .body("content[0].itensPedido", hasSize(2))
        .body("content[0].itensPedido[0].id", equalTo("00000000-0000-0000-0000-00000000c101"))
        .body("content[0].itensPedido[0].skuExterno", equalTo("SKU-TNS-001"))
        .body("content[0].itensPedido[0].nomeProduto", equalTo("Tenis Runner Pro"))
        .body("content[0].itensPedido[0].categoria", equalTo("Tenis"))
        .body("content[0].itensPedido[0].tamanho", equalTo(40))
        .body("content[0].itensPedido[0].cor", equalTo("Preto"))
        .body("content[0].itensPedido[0].precoUnitario", equalTo(199.9f))
        .body("content[0].itensPedido[0].quantidade", equalTo(1))
        .body("content[0].itensPedido[1].skuExterno", equalTo("SKU-SLT-002"));
  }

  @Test
  @DisplayName("pagina o histórico do cliente, mais recentes primeiro")
  void paginaOHistorico() {
    given()
        .queryParam("page", 1)
        .queryParam("size", 2)
        .when()
        .get(VENDAS_DO_CLIENTE, "11111111111")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body("totalElements", equalTo(5))
        .body("totalPages", equalTo(3))
        .body("content.pedidoExternoId", contains("pedido-a3", "pedido-a2"))
        .body("content[0].itensPedido", empty());
  }

  @Test
  @DisplayName("CPF sem compras devolve página vazia, não 404")
  void cpfSemCompras() {
    given()
        .when()
        .get(VENDAS_DO_CLIENTE, "99999999999")
        .then()
        .statusCode(Status.OK.getStatusCode())
        .body("content", empty())
        .body("totalElements", equalTo(0))
        .body("totalPages", equalTo(0));
  }

  @Test
  @DisplayName("CPF inválido no caminho é recusado com 400 e code CPF_INVALIDO")
  void cpfInvalido() {
    given()
        .when()
        .get(VENDAS_DO_CLIENTE, "abc")
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("code", equalTo(CodigoErro.CPF_INVALIDO.name()));
  }
}
