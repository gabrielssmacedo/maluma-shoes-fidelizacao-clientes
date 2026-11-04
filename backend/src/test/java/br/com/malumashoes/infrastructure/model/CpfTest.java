package br.com.malumashoes.infrastructure.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkiverse.httpproblem.HttpProblem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

  @ParameterizedTest
  @ValueSource(strings = {"12345678901", "123.456.789-01", " 123.456.789-01 ", "123 456 789 01"})
  @DisplayName("normaliza a entrada com ou sem pontuação para os 11 dígitos")
  void normaliza(final String entrada) {
    assertEquals("12345678901", Cpf.of(entrada).digitos());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "123", "123456789012", "1234567890a", "123/456/789-01"})
  @DisplayName("recusa com 400 o que não tem 11 dígitos")
  void recusaInvalido(final String entrada) {
    final HttpProblem erro = assertThrows(HttpProblem.class, () -> Cpf.of(entrada));
    assertEquals(400, erro.getStatusCode());
    assertEquals("CPF_INVALIDO", erro.getParameters().get("code"));
  }

  @Test
  @DisplayName("o opcional ausente ou em branco vira null")
  void opcional() {
    assertNull(Cpf.ofNullable(null));
    assertNull(Cpf.ofNullable("  "));
    assertEquals("12345678901", Cpf.ofNullable("12345678901").digitos());
  }

  @Test
  @DisplayName("no log aparece só o final do CPF")
  void mascarado() {
    assertEquals("***.***.**9-01", Cpf.of("12345678901").toString());
  }
}
