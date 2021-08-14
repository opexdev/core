package co.nilin.mixchange.port.order.kafka.config


import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient


@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.mixchange"])
class PostgresConfig(db: DatabaseClient) {

    init {
        val initDb = db.sql {
            """ 
            """
        }
        initDb // initialize the database
                .then()
                .subscribe() // execute
    }
}
