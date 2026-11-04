package br.com.malumashoes.infrastructure.model.rest.pagina;

import br.com.malumashoes.infrastructure.model.rest.erro.CodigoErro;
import io.quarkus.panache.common.Page;

/**
 * Nomes e validação dos query params {@code page}/{@code size} dos endpoints paginados por número
 * de página (a tabela do frontend mostra "página X de Y", por isso não é cursor).
 *
 * <p>Os dois chegam como texto e são convertidos aqui: como {@code Integer} no JAX-RS, um {@code
 * page=abc} virava 404 em vez de 400. E o {@code page} é limitado para o deslocamento ({@code page
 * * size}, um {@code int} no Panache) não estourar e virar 500.
 */
public final class PaginaParams {

  public static final String PAGE_PARAM_NAME = "page";
  public static final String SIZE_PARAM_NAME = "size";
  public static final int PRIMEIRA_PAGINA = 0;
  public static final int DEFAULT_SIZE = 20;
  public static final int MAX_SIZE = 100;

  static final String PAGE_INVALIDO_MESSAGE =
      "O page precisa ser um número inteiro a partir de 0 (e no máximo %d com size %d).";
  static final String SIZE_INVALIDO_MESSAGE = "O size precisa ser um número inteiro.";

  private PaginaParams() {}

  /**
   * A página pedida, já validada: {@code size} primeiro, porque o limite do {@code page} usa ele.
   */
  public static Page of(final String page, final String size) {
    final int tamanho = normalizeSize(size);
    return Page.of(normalizePage(page, tamanho), tamanho);
  }

  /** Ausente vira a primeira página; negativa, não numérica ou grande demais é recusada com 400. */
  static int normalizePage(final String page, final int size) {
    final int maiorPagina = Integer.MAX_VALUE / size;
    if (null == page || page.isBlank()) {
      return PRIMEIRA_PAGINA;
    }
    try {
      final int indice = Integer.parseInt(page.strip());
      if (PRIMEIRA_PAGINA <= indice && indice <= maiorPagina) {
        return indice;
      }
    } catch (final NumberFormatException e) {
      // cai no 400 abaixo
    }
    throw CodigoErro.PAGINA_INVALIDA.requisicaoInvalida(
        PAGE_INVALIDO_MESSAGE.formatted(maiorPagina, size));
  }

  /** Ausente ou menor que 1 vira o padrão (20); acima do máximo, o máximo (100). */
  static int normalizeSize(final String size) {
    if (null == size || size.isBlank()) {
      return DEFAULT_SIZE;
    }
    final long valor;
    try {
      valor = Long.parseLong(size.strip());
    } catch (final NumberFormatException e) {
      throw CodigoErro.PAGINA_INVALIDA.requisicaoInvalida(SIZE_INVALIDO_MESSAGE);
    }
    if (1 > valor) {
      return DEFAULT_SIZE;
    }
    return (int) Math.min(valor, MAX_SIZE);
  }
}
