package br.com.malumashoes.infrastructure.security;

import br.com.malumashoes.infrastructure.model.rest.erro.CodigoErro;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response.Status;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Autenticação simples por chave de API: toda rota da API ({@code /api/v1/*}) exige o header
 * {@value #HEADER} com o valor de {@code app.api-key} (variável {@code API_KEY} do {@code .env}).
 * Health e Swagger ({@code /q/*}) ficam fora, porque não são recursos JAX-RS.
 *
 * <p>É uma chave única compartilhada pelo frontend, suficiente para o protótipo; usuários com login
 * e perfis (OIDC/JWT) ficam para quando o CRM tiver telas de acesso.
 */
public class ApiKeyFilter {

  public static final String HEADER = "X-API-Key";

  static final String CHAVE_AUSENTE_MESSAGE = "Envie a chave de API no header " + HEADER + ".";

  private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

  private final byte[] chaveEsperada;

  public ApiKeyFilter(@ConfigProperty(name = "app.api-key") final String chave) {
    this.chaveEsperada = chave.getBytes(StandardCharsets.UTF_8);
  }

  @ServerRequestFilter
  public void exigirChave(final ContainerRequestContext request) {
    final String recebida = request.getHeaderString(HEADER);
    // Comparação em tempo constante: não revela quantos caracteres da chave estão certos.
    if (null == recebida
        || !MessageDigest.isEqual(chaveEsperada, recebida.getBytes(StandardCharsets.UTF_8))) {
      log.warn("Requisição recusada sem chave de API válida: {}", request.getUriInfo().getPath());
      throw CodigoErro.NAO_AUTENTICADO.problema(Status.UNAUTHORIZED, CHAVE_AUSENTE_MESSAGE);
    }
  }
}
