package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.WithdrawStatus
import java.math.BigDecimal
import java.time.LocalDateTime

interface WithdrawRequestEventProducer {

    suspend fun send(
        uuid: String,
        withdrawId: Long?,
        currency: String,
        amount: BigDecimal,
        withdrawStatus: WithdrawStatus,
        createDate: LocalDateTime
    )
}