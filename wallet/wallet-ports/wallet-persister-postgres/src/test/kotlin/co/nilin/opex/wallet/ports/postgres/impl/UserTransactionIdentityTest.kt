package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.ports.postgres.dao.UserTransactionRepository
import co.nilin.opex.wallet.ports.postgres.model.UserTransactionModel
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class UserTransactionIdentityTest {
    @Test
    fun `preserves row identity posting time and operation references on save`() = runBlocking<Unit> {
        val repository = mockk<UserTransactionRepository>()
        val saved = slot<UserTransactionModel>()
        every { repository.save(capture(saved)) } answers { Mono.just(saved.captured) }
        val manager = UserTransactionManagerImpl(repository, mockk())
        val row = UserTransaction(1, 42, "ETH", BigDecimal.TEN, BigDecimal.ONE,
            UserTransactionCategory.TRADE, date = LocalDateTime.of(2026, 11, 18, 12, 30),
            operationId = "accountant-trade:execution-1", financialActionId = "action-1")
        manager.save(row)
        assertThat(saved.captured.uuid).isEqualTo(row.uuid)
        assertThat(saved.captured.date).isEqualTo(row.date)
        assertThat(saved.captured.txId).isEqualTo(42)
        assertThat(saved.captured.operationId).isEqualTo(row.operationId)
        assertThat(saved.captured.financialActionId).isEqualTo(row.financialActionId)
    }
}
