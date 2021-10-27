package co.nilin.opex.port.accountant.postgres.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient


@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresConfig(db: DatabaseClient, @Value("classpath:scheme.sql") private val schemeResource: Resource) {
    init {
        val reader = schemeResource.inputStream.reader()
        val scheme = reader.readText().trim()
        reader.close()
        val initDb = db.sql { scheme }
        initDb // initialize the database
            .then()
            .subscribe() // execute
    }
}
