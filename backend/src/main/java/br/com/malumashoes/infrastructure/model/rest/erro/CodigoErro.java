package br.com.malumashoes.infrastructure.model.rest.erro;

import io.quarkiverse.httpproblem.HttpProblem;
import jakarta.ws.rs.core.Response.Status;

/**
 * Valores do campo {@code code} de toda resposta de erro, uma extensão RFC 9457 que o frontend
 * compara em vez de interpretar o texto de {@code detail}. Os códigos de negócio são lançados via
 * {@link #problema}; o restante recebe um código genérico pelo status em {@link
 * CodigoErroPostProcessor}. Renomear um valor quebra o frontend: só acrescente.
 */
public enum CodigoErro {
  CPF_INVALIDO("O CPF precisa ter 11 dígitos (com ou sem pontuação) (400)."),
  DATA_INVALIDA("A data precisa estar no formato yyyy-MM-dd (400)."),
  PERIODO_INVALIDO("A data_inicio não pode ser depois da data_fim (400)."),
  PAGINA_INVALIDA(
      "O page ou o size não é um inteiro válido, ou o page é negativo ou grande demais (400)."),
  NAO_AUTENTICADO("Falta o header X-API-Key ou a chave está errada (401)."),
  REQUISICAO_INVALIDA("Erro genérico de requisição (4xx sem código próprio)."),
  NAO_ENCONTRADO("Rota ou recurso inexistente (404)."),
  ERRO_INTERNO("Falha inesperada do servidor (5xx).");

  public static final String CODE_PARAM = "code";

  private static final int PRIMEIRO_STATUS_DE_SERVIDOR = 500;

  private final String descricao;

  CodigoErro(final String descricao) {
    this.descricao = descricao;
  }

  public String descricao() {
    return descricao;
  }

  /** Código genérico para um erro que não trouxe código de negócio. */
  public static CodigoErro doStatus(final int status) {
    if (Status.NOT_FOUND.getStatusCode() == status) {
      return NAO_ENCONTRADO;
    }
    if (Status.UNAUTHORIZED.getStatusCode() == status) {
      return NAO_AUTENTICADO;
    }
    return status < PRIMEIRO_STATUS_DE_SERVIDOR ? REQUISICAO_INVALIDA : ERRO_INTERNO;
  }

  /** Problem JSON 400 com este código: parâmetro recusado como enviado. */
  public HttpProblem requisicaoInvalida(final String detail) {
    return problema(Status.BAD_REQUEST, detail);
  }

  /** Problem JSON com este código, para lançar no lugar de uma exceção JAX-RS sem código. */
  public HttpProblem problema(final Status status, final String detail) {
    return HttpProblem.builder()
        .withStatus(status)
        .withTitle(status.getReasonPhrase())
        .withDetail(detail)
        .with(CODE_PARAM, name())
        .build();
  }
}
