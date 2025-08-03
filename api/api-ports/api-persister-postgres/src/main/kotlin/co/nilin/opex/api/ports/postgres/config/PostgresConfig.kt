package co.nilin.opex.api.ports.postgres.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.JdbcTemplate
import java.nio.charset.StandardCharsets

@Configuration
@EnableJdbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresConfig(
    template: JdbcTemplate,
    @Value("classpath:schema.sql") private val schemaResource: Resource
) {
    init {
        val schema = schemaResource.inputStream
            .bufferedReader(StandardCharsets.UTF_8)
            .use { it.readText().trim() }

        val statements = schema.split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        statements.forEach {
            template.execute(it)
        }
    }
}
