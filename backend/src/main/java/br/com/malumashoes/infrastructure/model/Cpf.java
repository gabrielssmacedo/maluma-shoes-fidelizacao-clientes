package br.com.malumashoes.infrastructure.model;

import br.com.malumashoes.infrastructure.model.rest.erro.CodigoErro;
import java.util.regex.Pattern;

/**
 * CPF normalizado para os 11 dígitos, o formato gravado em {@code historico_vendas.cliente_cpf}.
 * Aceita a entrada com ou sem pontuação ({@code 123.456.789-01}). Não confere os dígitos
 * verificadores: a massa mockada do Sistema de Vendas gera CPFs que não passariam nessa conta, e a
 * busca só precisa achar o que foi gravado.
 */
public record Cpf(String digitos) {

  static final String CPF_INVALIDO_MESSAGE =
      "CPF inválido: informe os 11 dígitos, com ou sem pontuação.";

  private static final Pattern PONTUACAO = Pattern.compile("[.\\-\\s]");
  private static final Pattern ONZE_DIGITOS = Pattern.compile("\\d{11}");

  public Cpf {
    if (null == digitos || !ONZE_DIGITOS.matcher(digitos).matches()) {
      throw CodigoErro.CPF_INVALIDO.requisicaoInvalida(CPF_INVALIDO_MESSAGE);
    }
  }

  /** Remove a pontuação e valida; recusa com 400 o que não sobrar com 11 dígitos. */
  public static Cpf of(final String entrada) {
    if (null == entrada) {
      throw CodigoErro.CPF_INVALIDO.requisicaoInvalida(CPF_INVALIDO_MESSAGE);
    }
    return new Cpf(PONTUACAO.matcher(entrada).replaceAll(""));
  }

  /** Opcional: ausente ou em branco vira {@code null} (sem filtro). */
  public static Cpf ofNullable(final String entrada) {
    return null == entrada || entrada.isBlank() ? null : of(entrada);
  }

  /** Para log: só os 3 últimos dígitos, nunca o CPF inteiro (LGPD). */
  public String mascarado() {
    return "***.***.**" + digitos.charAt(8) + "-" + digitos.substring(9);
  }

  @Override
  public String toString() {
    return mascarado();
  }
}
