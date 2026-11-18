package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionType
import co.nilin.opex.eventlog.ports.postgres.config.PostgresConfig
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDateTime

@Testcontainers
@SpringBootTest(classes = [PostgresConfig::class, AccountingTransactionLogPersister::class])
class AccountingTransactionLogPersisterRoundTripIT {

    @Autowired
    private lateinit var persister: AccountingTransactionLogPersister

    @Test
    fun `postgres round-trip keeps replay idempotent despite scale and timestamp precision differences`() = runBlocking {
        val base = LocalDateTime.parse("2026-11-16T10:11:12.123456789")
        val first = baseEvent(
            amount = BigDecimal("20.00"),
            fee = BigDecimal("0.10"),
            principal = BigDecimal("19.90"),
            gross = BigDecimal("20.00"),
            occurredAt = base
        )
        val replay = baseEvent(
            amount = BigDecimal("20.00000000"),
            fee = BigDecimal("0.10000000"),
            principal = BigDecimal("19.90000000"),
            gross = BigDecimal("20.00000000"),
            occurredAt = base.withNano(123456001),
            operationCreatedAt = base.minusMinutes(1).withNano(111222333),
            lifecycleTransitionAt = base.withNano(123456001),
            completedAt = base.withNano(123456001)
        )

        val initialPersisted = saveWithSchemaRetry(first)
        val replayPersisted = persister.save(replay)

        assertThat(replayPersisted.eventId).isEqualTo(initialPersisted.eventId)
        assertThat(replayPersisted.amount.compareTo(first.amount)).isZero()
        assertThat(replayPersisted.fee!!.compareTo(first.fee!!)).isZero()
        assertThat(replayPersisted.grossAmount!!.compareTo(first.grossAmount!!)).isZero()
    }

    private suspend fun saveWithSchemaRetry(event: AccountingTransactionEvent): AccountingTransactionEvent {
        var lastError: Throwable? = null
        repeat(30) {
            try {
                return persister.save(event)
            } catch (ex: Throwable) {
                lastError = ex
                Thread.sleep(200)
            }
        }
        throw IllegalStateException("Failed to persist event after retries", lastError)
    }

    private fun baseEvent(
        amount: BigDecimal,
        fee: BigDecimal?,
        principal: BigDecimal?,
        gross: BigDecimal?,
        occurredAt: LocalDateTime,
        operationCreatedAt: LocalDateTime? = occurredAt.minusMinutes(1),
        lifecycleTransitionAt: LocalDateTime? = occurredAt,
        completedAt: LocalDateTime? = occurredAt
    ): AccountingTransactionEvent {
        return AccountingTransactionEvent(
            txId = "withdraw-lifecycle:withdraw-100:DONE",
            eventId = "withdraw-lifecycle:withdraw-100:DONE",
            userId = "user-100",
            type = AccountingTransactionType.WITHDRAW,
            symbol = "USDT",
            asset = "USDT",
            amount = amount,
            fee = fee,
            feeAsset = "USDT",
            principalAmount = principal,
            grossAmount = gross,
            withdrawStatus = "DONE",
            transferCategory = "WITHDRAW_ACCEPT",
            operationId = "withdraw-100",
            businessReference = "wallet:withdraw:withdraw-100:DONE",
            walletTransactionRef = "tx-100",
            recordRole = "WITHDRAW_LIFECYCLE",
            source = "CORE_WALLET_WITHDRAW",
            referenceId = "withdraw-100",
            side = "OUT",
            price = null,
            quantity = null,
            operationCreatedAt = operationCreatedAt,
            lifecycleTransitionAt = lifecycleTransitionAt,
            completedAt = completedAt,
            occurredAt = occurredAt
        )
    }

    companion object {
        @Container
        private val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine")
            .apply {
                withDatabaseName("eventlog_test")
                withUsername("postgres")
                withPassword("postgres")
            }

        @JvmStatic
        @DynamicPropertySource
        fun registerDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.r2dbc.username") { postgres.username }
            registry.add("spring.r2dbc.password") { postgres.password }
        }
    }
}
