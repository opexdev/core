package co.nilin.opex.wallet.core.spi
import java.math.BigDecimal
import java.time.LocalDateTime

interface DepositEventSubmitter {
    suspend fun send(
        uuid: String,
        depositRef: String?,
        currency: String,
        amount: BigDecimal,
        createDate: LocalDateTime?= LocalDateTime.now()
    )
}