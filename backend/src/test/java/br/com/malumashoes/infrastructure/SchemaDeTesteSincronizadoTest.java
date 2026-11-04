package br.com.malumashoes.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O schema do Postgres dos testes (src/test/resources/db/schema.sql) é uma cópia do DDL de
 * postgres-init/02-init-app-tables.sh. Se alguém mudar a tabela lá, este teste reprova até a cópia
 * ser atualizada, para os testes nunca rodarem contra uma tabela diferente da real.
 */
class SchemaDeTesteSincronizadoTest {

  private static final Path SCRIPT_ORIGINAL =
      Path.of("..", "postgres-init", "02-init-app-tables.sh");
  private static final Path COPIA_DE_TESTE =
      Path.of("src", "test", "resources", "db", "schema.sql");
  private static final Pattern DDL =
      Pattern.compile("CREATE (TABLE|INDEX) IF NOT EXISTS[^;]*;", Pattern.CASE_INSENSITIVE);

  @Test
  @DisplayName("o schema dos testes é igual ao DDL de postgres-init")
  void copiaIgualAoOriginal() throws IOException {
    assertEquals(ddl(SCRIPT_ORIGINAL), ddl(COPIA_DE_TESTE));
  }

  private static String ddl(final Path arquivo) throws IOException {
    final Matcher comandos = DDL.matcher(Files.readString(arquivo, StandardCharsets.UTF_8));
    final StringBuilder normalizado = new StringBuilder();
    while (comandos.find()) {
      normalizado.append(comandos.group().replaceAll("\\s+", " ")).append('\n');
    }
    return normalizado.toString();
  }
}
