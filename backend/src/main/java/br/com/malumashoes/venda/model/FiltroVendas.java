package br.com.malumashoes.venda.model;

import br.com.malumashoes.infrastructure.model.Cpf;
import br.com.malumashoes.infrastructure.model.rest.erro.CodigoErro;
import br.com.malumashoes.venda.model.entity.HistoricoVenda_;
import io.quarkus.panache.common.Parameters;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtros opcionais da listagem de vendas. As datas chegam como dia ({@code yyyy-MM-dd}) e valem no
 * fuso da loja: {@code data_fim} entra inteira (até 23:59:59), por isso vira o início do dia
 * seguinte, exclusivo.
 */
public record FiltroVendas(Cpf cpf, OffsetDateTime inicio, OffsetDateTime fimExclusivo) {

  static final String DATA_INVALIDA_MESSAGE = "%s inválida: use o formato yyyy-MM-dd.";
  static final String PERIODO_INVALIDO_MESSAGE = "A data_inicio não pode ser depois da data_fim.";

  private static final String CPF_PARAM = "cpf";
  private static final String INICIO_PARAM = "inicio";
  private static final String FIM_PARAM = "fim";

  public static FiltroVendas of(
      final String cpf, final String dataInicio, final String dataFim, final ZoneId zona) {
    final LocalDate inicio = parseData(dataInicio, "data_inicio");
    final LocalDate fim = parseData(dataFim, "data_fim");
    if (null != inicio && null != fim && inicio.isAfter(fim)) {
      throw CodigoErro.PERIODO_INVALIDO.requisicaoInvalida(PERIODO_INVALIDO_MESSAGE);
    }
    return new FiltroVendas(
        Cpf.ofNullable(cpf),
        null == inicio ? null : inicio.atStartOfDay(zona).toOffsetDateTime(),
        null == fim ? null : fim.plusDays(1).atStartOfDay(zona).toOffsetDateTime());
  }

  public static FiltroVendas doCliente(final Cpf cpf) {
    return new FiltroVendas(cpf, null, null);
  }

  /** Cláusula HQL só com os filtros informados; vazia lista tudo. */
  public String where() {
    final List<String> condicoes = new ArrayList<>();
    if (null != cpf) {
      condicoes.add(HistoricoVenda_.CLIENTE_CPF + " = :" + CPF_PARAM);
    }
    if (null != inicio) {
      condicoes.add(HistoricoVenda_.DATA_COMPRA + " >= :" + INICIO_PARAM);
    }
    if (null != fimExclusivo) {
      condicoes.add(HistoricoVenda_.DATA_COMPRA + " < :" + FIM_PARAM);
    }
    return String.join(" and ", condicoes);
  }

  /** Os valores de {@link #where()}, sempre como parâmetros (nunca concatenados no HQL). */
  public Parameters parametros() {
    final Parameters parametros = new Parameters();
    if (null != cpf) {
      parametros.and(CPF_PARAM, cpf.digitos());
    }
    if (null != inicio) {
      parametros.and(INICIO_PARAM, inicio);
    }
    if (null != fimExclusivo) {
      parametros.and(FIM_PARAM, fimExclusivo);
    }
    return parametros;
  }

  private static LocalDate parseData(final String valor, final String nome) {
    if (null == valor || valor.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(valor.strip());
    } catch (final DateTimeParseException e) {
      throw CodigoErro.DATA_INVALIDA.requisicaoInvalida(DATA_INVALIDA_MESSAGE.formatted(nome));
    }
  }
}
