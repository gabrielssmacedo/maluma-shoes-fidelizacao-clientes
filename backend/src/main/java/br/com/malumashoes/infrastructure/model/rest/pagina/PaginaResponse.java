package br.com.malumashoes.infrastructure.model.rest.pagina;

import java.util.List;
import java.util.function.Function;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Uma página de resultados, com o total para o frontend montar a paginação. */
@Schema(description = "Página de resultados.")
public record PaginaResponse<T>(
    @Schema(description = "Itens desta página.") List<T> content,
    @Schema(description = "Número desta página, a partir de 0.", examples = "0") int page,
    @Schema(description = "Itens por página pedidos.", examples = "20") int size,
    @Schema(description = "Total de itens em todas as páginas.", examples = "20000")
        long totalElements,
    @Schema(description = "Total de páginas.", examples = "1000") int totalPages) {

  public PaginaResponse {
    content = List.copyOf(content);
  }

  public static <E, T> PaginaResponse<T> of(
      final List<E> itens,
      final int page,
      final int size,
      final long totalElements,
      final Function<E, T> conversor) {
    final int totalPages = (int) ((totalElements + size - 1) / size);
    return new PaginaResponse<>(
        itens.stream().map(conversor).toList(), page, size, totalElements, totalPages);
  }
}
