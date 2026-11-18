package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.model.WithdrawStatus
import co.nilin.opex.accountant.core.spi.UserWithdrawVolumePersister
import co.nilin.opex.accountant.ports.kafka.listener.inout.WithdrawRequestEvent
import io.mockk.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class AcceptedWithdrawalVolumeTest {
    @Test
    fun `approval does not add to withdrawal limit volume again`() {
        val volumes = mockk<UserWithdrawVolumePersister>()
        WithdrawRequestEventListener(volumes).onEvent(WithdrawRequestEvent(
            uuid = "customer", withdrawId = 1L, currency = "USDT", amount = BigDecimal("101"),
            status = WithdrawStatus.ACCEPTED, createDate = LocalDateTime.parse("2026-11-16T10:00:00")
        ), 0, 1, 0)
        coVerify(exactly = 0) { volumes.update(any(), any(), any(), any(), any()) }
    }
}
