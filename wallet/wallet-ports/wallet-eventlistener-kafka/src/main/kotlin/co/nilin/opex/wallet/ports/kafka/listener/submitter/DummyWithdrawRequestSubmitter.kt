package co.nilin.opex.wallet.ports.kafka.listener.submitter

import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.spi.WithdrawRequestEventSubmitter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(name = ["app.withdraw.limit.enabled"], havingValue = "false")
class DummyWithdrawRequestSubmitter(
) :
    WithdrawRequestEventSubmitter {
    override suspend fun send(
        uuid: String,
        withdrawId: Long?,
        currency: String,
        amount: BigDecimal,
        withdrawStatus: WithdrawStatus,
        createDate: LocalDateTime
    ) {

    }
}