package br.com.malumashoes.cliente.controller;

import br.com.malumashoes.infrastructure.model.rest.docs.CommonApiDocs;
import br.com.malumashoes.infrastructure.model.rest.pagina.PaginaParams;
import br.com.malumashoes.infrastructure.model.rest.pagina.PaginaResponse;
import br.com.malumashoes.venda.model.rest.response.VendaDetalheResponse;
import br.com.malumashoes.venda.service.VendaService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(ClienteVendasResource.PATH)
@WithSession
@SecurityRequirement(name = CommonApiDocs.SECURITY_SCHEME)
@Tag(name = "Clientes", description = "Atalhos do CRM a partir de um cliente.")
public class ClienteVendasResource {

  public static final String PATH = "/clientes/{cpf}/vendas";

  private final VendaService vendas;

  @Inject
  public ClienteVendasResource(final VendaService vendas) {
    this.vendas = vendas;
  }

  @GET
  @Operation(
      summary = "Listar o histórico de compras de um cliente",
      description =
          "Todas as vendas do CPF, da mais recente para a mais antiga, com os itens de cada pedido."
              + " CPF sem compras devolve página vazia.")
  @APIResponse(
      responseCode = CommonApiDocs.HTTP_200,
      description = CommonApiDocs.PAGINA_RETORNADA_COM_SUCESSO)
  @APIResponse(responseCode = CommonApiDocs.HTTP_401, description = CommonApiDocs.NAO_AUTENTICADO)
  @APIResponse(
      responseCode = CommonApiDocs.HTTP_400,
      description = CommonApiDocs.PARAMETRO_INVALIDO)
  public Uni<PaginaResponse<VendaDetalheResponse>> listar(
      @Parameter(description = CommonApiDocs.CPF_PARAM_DESCRIPTION) @PathParam("cpf")
          final String cpf,
      @Parameter(description = CommonApiDocs.PAGE_PARAM_DESCRIPTION)
          @QueryParam(PaginaParams.PAGE_PARAM_NAME)
          final String page,
      @Parameter(description = CommonApiDocs.SIZE_PARAM_DESCRIPTION)
          @QueryParam(PaginaParams.SIZE_PARAM_NAME)
          final String size) {
    return vendas.listarDoCliente(cpf, page, size);
  }
}
