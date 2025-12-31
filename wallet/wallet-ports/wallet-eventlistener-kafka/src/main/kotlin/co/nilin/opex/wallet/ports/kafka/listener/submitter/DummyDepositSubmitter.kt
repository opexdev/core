package co.nilin.opex.wallet.ports.kafka.listener.submitter

import co.nilin.opex.wallet.core.spi.DepositEventSubmitter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(name = ["app.deposit.snapshot.enabled"], havingValue = "false")
class DummyDepositSubmitter(
) : DepositEventSubmitter {
    override suspend fun send(
        uuid: String,
        depositRef: String?,
        currency: String,
        amount: BigDecimal,
        createDate: LocalDateTime?
    ) {

    }
}