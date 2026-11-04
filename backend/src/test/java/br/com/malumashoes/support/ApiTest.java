package br.com.malumashoes.support;

import br.com.malumashoes.infrastructure.security.ApiKeyFilter;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base dos testes de endpoint: toda requisição leva a chave de API de teste ({@code
 * %test.app.api-key}). Teste que precisa chamar sem chave usa {@link #semChave()}.
 */
public abstract class ApiTest {

  public static final String CHAVE_DE_TESTE = "chave-de-teste";

  @BeforeEach
  void comChave() {
    RestAssured.requestSpecification =
        new RequestSpecBuilder().addHeader(ApiKeyFilter.HEADER, CHAVE_DE_TESTE).build();
  }

  @AfterEach
  void limpar() {
    RestAssured.requestSpecification = null;
  }

  protected static void semChave() {
    RestAssured.requestSpecification = null;
  }
}
