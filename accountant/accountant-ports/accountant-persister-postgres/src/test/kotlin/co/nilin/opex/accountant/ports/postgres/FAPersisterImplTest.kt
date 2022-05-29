package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRepository
import co.nilin.opex.accountant.ports.postgres.impl.FinancialActionPersisterImpl
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import java.time.LocalDateTime

class FAPersisterImplTest {

    private val financialActionRepository = mockk<FinancialActionRepository>()
    private val faPersister = FinancialActionPersisterImpl(financialActionRepository)

    private val fa = FinancialAction(
        null,
        TradeEvent::class.java.name,
        "trade_id",
        "BTC_USDT",
        10000.0.toBigDecimal(),
        "user_parent",
        "main",
        "system",
        "main",
        LocalDateTime.now()
    )

    init {
        coEvery { financialActionRepository.saveAll(emptyList()) } returns Flux.empty()
    }

}