package co.nilin.opex.price.ports.postgres.support

import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Bare Spring Boot app used only by integration tests in this module. Scanning is rooted at
 * `co.nilin.opex.price.ports.postgres` so it picks up [co.nilin.opex.price.ports.postgres.config.PostgresConfig]
 * (Flyway migrations + `@EnableR2dbcRepositories`) and the DAO interfaces, without pulling in
 * anything from `price-management-app` (vault, consul, kafka, security, ...).
 */
@SpringBootApplication(scanBasePackages = ["co.nilin.opex.price.ports.postgres"])
class PostgresTestApp
