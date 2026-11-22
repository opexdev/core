package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.model.ReservedTransferModel
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class ReservedTransferValidityTest {
    @Test
    fun `only unexpired created reservations can execute including those without expiry`() = runBlocking<Unit> {
        val repository = mockk<ReservedTransferRepository>()
        val manager = ReservedTransferImpl(repository, mockk())
        val now = LocalDateTime.now()
        for (expiry in listOf(now.plusHours(1), now.minusHours(1), null)) {
            for (status in ReservedStatus.values()) {
                every { repository.findByReserveNumber("r-1") } returns Mono.just(ReservedTransferModel(
                    1, "r-1", "USDT", "IRT", WalletType.MAIN, "customer", WalletType.MAIN, "recipient",
                    BigDecimal.TEN, BigDecimal("1500000"), reserveDate = now, expDate = expiry,
                    status = status, rate = BigDecimal("150000"), applicator = "admin"))
                val reservation = manager.fetchValidReserve("r-1")
                if (status == ReservedStatus.Created && (expiry == null || expiry > now)) {
                    assertThat(reservation!!.id).isEqualTo(1)
                    assertThat(reservation.rate).isEqualByComparingTo("150000")
                    assertThat(reservation.applicator).isEqualTo("admin")
                } else assertThat(reservation).isNull()
            }
        }
    }
}
