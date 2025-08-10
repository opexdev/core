package co.nilin.opex.wallet.app

import com.zaxxer.hikari.HikariDataSource
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

@TestConfiguration
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresEnabledTest(@Value("\${testcontainers.db.name}") private val name: String,
                          @Value("\${testcontainers.db.username}") private val username: String,
                          @Value("\${testcontainers.db.password}") private val password: String,
                          @Value("\${testcontainers.db.version}") private val version: String) {
    private val logger = LoggerFactory.getLogger(PostgresEnabledTest::class.java)

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun postgresContainer(): PostgreSQLContainer<*> =
            PostgreSQLContainer<Nothing>(version).apply {
                withDatabaseName(name)
                withUsername(username)
                withPassword(password)
            }

    @Bean
    fun dataSource(container: PostgreSQLContainer<*>): DataSource {
        return HikariDataSource().apply {
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
        }
    }

    @Bean
    fun connectionFactory(container: PostgreSQLContainer<*>): ConnectionFactory {
        val r2dbcUrl = "r2dbc:postgresql://${container.host}:${container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)}/${container.databaseName}"
        val options = ConnectionFactoryOptions.parse(r2dbcUrl).mutate()
                .option(ConnectionFactoryOptions.USER, container.username)
                .option(ConnectionFactoryOptions.PASSWORD, container.password)
                .build()
        return ConnectionFactories.get(options)
    }

    @Bean
    fun flyway(dataSource: DataSource): Flyway {
        val flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load()
        try {
            retry(6, 5000) { flyway.migrate() }
        } catch (e: Exception) {
            logger.error("❌ Flyway migration failed", e)
        }
        return flyway
    }

    fun retry(times: Int, delayMs: Long, block: () -> Unit) {
        var attempt = 0
        while (true) {
            try {
                block()
                return
            } catch (e: Exception) {
                if (++attempt >= times) throw e
                Thread.sleep(delayMs)
            }
        }
    }

}