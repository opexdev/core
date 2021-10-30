package co.nilin.opex.port.accountant.postgres.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@Profile("demo")
class PostgresDemoConfig(db: DatabaseClient, @Value("classpath:schema-demo.sql") private val schemeResource: Resource) {
    init {
        val reader = schemeResource.inputStream.reader()
        val schema = reader.readText().trim()
        reader.close()
        val initDb = db.sql { schema }
        initDb // initialize the database
                .then()
                .subscribe() // execute
    }
}
