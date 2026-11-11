package co.nilin.opex.wallet.ports.postgres.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager

/**
 * Pins the transaction manager to the reactive one, which is the only kind that can roll back the
 * R2DBC work every `@Transactional` suspend function in this module does.
 *
 * Boot would normally settle this on its own, but only by accident here. The module declares both
 * `spring.r2dbc` and `spring.datasource` plus `spring-boot-starter-jdbc`, and the two candidate
 * auto-configurations do not exclude each other: the JDBC one asks for a missing `TransactionManager`
 * while the R2DBC one only asks for a missing `ReactiveTransactionManager`. What keeps the JDBC
 * manager away today is that no connection pool reaches the runtime classpath - HikariCP arrives
 * only in test scope - so no `DataSource` bean is built and the JDBC auto-configuration finds no
 * candidate. Putting any pool on the compile classpath would quietly produce two managers.
 *
 * Declaring this bean satisfies both conditions at once, so exactly one manager exists regardless of
 * what the dependency graph does later. Nothing in this module talks JDBC at runtime: the
 * `spring.datasource` properties are only read by Flyway, which builds its own DataSource.
 */
@Configuration
class TransactionConfig {

    @Bean("transactionManager")
    fun reactiveTransactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}
