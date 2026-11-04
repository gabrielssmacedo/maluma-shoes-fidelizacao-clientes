package br.com.malumashoes.infrastructure.model.rest.docs;

/** Textos de documentação OpenAPI repetidos nos endpoints. */
public final class CommonApiDocs {

  public static final String HTTP_200 = "200";
  public static final String HTTP_400 = "400";
  public static final String HTTP_401 = "401";

  /** Nome padrão do esquema que quarkus.smallrye-openapi.security-scheme cria. */
  public static final String SECURITY_SCHEME = "SecurityScheme";

  public static final String NAO_AUTENTICADO =
      "Sem o header X-API-Key ou com a chave errada (code: NAO_AUTENTICADO).";
  public static final String PAGINA_RETORNADA_COM_SUCESSO = "Página retornada com sucesso.";
  public static final String PARAMETRO_INVALIDO =
      "Parâmetro inválido (code: CPF_INVALIDO, DATA_INVALIDA, PERIODO_INVALIDO ou"
          + " PAGINA_INVALIDA).";
  public static final String PAGE_PARAM_DESCRIPTION = "Número da página, a partir de 0. Padrão: 0.";
  public static final String SIZE_PARAM_DESCRIPTION = "Itens por página (máx. 100). Padrão: 20.";
  public static final String CPF_PARAM_DESCRIPTION =
      "CPF do cliente, 11 dígitos com ou sem pontuação.";

  private CommonApiDocs() {}
}
