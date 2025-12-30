package co.nilin.opex.api.ports.postgres.config

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
@Profile("!test")
class PostgresConfig {
    private val logger = LoggerFactory.getLogger(PostgresConfig::class.java)

    init {
        logger.info("🔍 PostgresConfig loaded")
    }

    @Bean
    fun flywayConfig(
        @Value("\${spring.datasource.url}") url: String,
        @Value("\${spring.datasource.username}") user: String,
        @Value("\${spring.datasource.password}") password: String
    ): Flyway? {
        val flyway: Flyway = Flyway.configure()
            .dataSource(url, user, password)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("1")
            .load()
        try {
            retry(6, 5000) {
                flyway.migrate()
            }
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