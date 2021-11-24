package co.nilin.opex.wallet.ports.postgres.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresConfig(
    db: DatabaseClient,
    @Value("classpath:schema.sql") private val schemaResource: Resource,
    @Value("classpath:data.sql") private val dataResource: Resource?
) {
    init {
        val schemaReader = schemaResource.inputStream.reader()
        val schema = schemaReader.readText().trim()
        schemaReader.close()
        val dataReader = dataResource?.inputStream?.reader()
        val data = dataReader?.readText()?.trim() ?: ""
        dataReader?.close()
        val initDb = db.sql { schema.plus(data) }
        initDb // initialize the database
            .then()
            .subscribe() // execute
    }
}
