package br.com.malumashoes.venda.controller;

import br.com.malumashoes.infrastructure.model.rest.docs.CommonApiDocs;
import br.com.malumashoes.infrastructure.model.rest.pagina.PaginaParams;
import br.com.malumashoes.infrastructure.model.rest.pagina.PaginaResponse;
import br.com.malumashoes.venda.model.rest.response.VendaResumoResponse;
import br.com.malumashoes.venda.service.VendaService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(VendaResource.PATH)
@WithSession
@SecurityRequirement(name = CommonApiDocs.SECURITY_SCHEME)
@Tag(name = "Vendas", description = "Histórico geral de vendas para a tabela principal do CRM.")
public class VendaResource {

  public static final String PATH = "/vendas";

  private final VendaService vendas;

  @Inject
  public VendaResource(final VendaService vendas) {
    this.vendas = vendas;
  }

  @GET
  @Operation(
      summary = "Listar o histórico de vendas",
      description =
          "Vendas resumidas (sem os itens do pedido), da mais recente para a mais antiga. As datas"
              + " valem no fuso de São Paulo, e a data_fim entra inteira.")
  @APIResponse(
      responseCode = CommonApiDocs.HTTP_200,
      description = CommonApiDocs.PAGINA_RETORNADA_COM_SUCESSO)
  @APIResponse(responseCode = CommonApiDocs.HTTP_401, description = CommonApiDocs.NAO_AUTENTICADO)
  @APIResponse(
      responseCode = CommonApiDocs.HTTP_400,
      description = CommonApiDocs.PARAMETRO_INVALIDO)
  public Uni<PaginaResponse<VendaResumoResponse>> listar(
      @Parameter(description = CommonApiDocs.PAGE_PARAM_DESCRIPTION)
          @QueryParam(PaginaParams.PAGE_PARAM_NAME)
          final String page,
      @Parameter(description = CommonApiDocs.SIZE_PARAM_DESCRIPTION)
          @QueryParam(PaginaParams.SIZE_PARAM_NAME)
          final String size,
      @Parameter(description = CommonApiDocs.CPF_PARAM_DESCRIPTION) @QueryParam("cpf")
          final String cpf,
      @Parameter(description = "Primeiro dia do período (yyyy-MM-dd).") @QueryParam("data_inicio")
          final String dataInicio,
      @Parameter(description = "Último dia do período, incluído (yyyy-MM-dd).")
          @QueryParam("data_fim")
          final String dataFim) {
    return vendas.listar(page, size, cpf, dataInicio, dataFim);
  }
}
