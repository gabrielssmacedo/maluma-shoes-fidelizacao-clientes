package br.com.malumashoes.infrastructure.model.rest.erro;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.ProblemContext;
import io.quarkiverse.httpproblem.postprocessing.ProblemPostProcessor;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Garante o campo {@code code} em cada Problem JSON: o erro que já traz um código de negócio
 * ({@link CodigoErro#problema}) passa intacto; os demais (exceções JAX-RS e as geradas pelo próprio
 * Quarkus, como rota inexistente) recebem o código genérico do status.
 */
@ApplicationScoped
public class CodigoErroPostProcessor implements ProblemPostProcessor {

  @Override
  public HttpProblem apply(final HttpProblem problem, final ProblemContext context) {
    if (problem.getParameters().containsKey(CodigoErro.CODE_PARAM)) {
      return problem;
    }
    return HttpProblem.builder(problem)
        .with(CodigoErro.CODE_PARAM, CodigoErro.doStatus(problem.getStatusCode()).name())
        .build();
  }
}
