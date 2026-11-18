package co.nilin.opex.wallet.ports.kafka.listener.submitter

import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.spi.WithdrawRequestEventSubmitter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(name = ["app.withdraw.event.enabled"], havingValue = "false")
class DummyWithdrawRequestSubmitter(
) :
    WithdrawRequestEventSubmitter {
    override suspend fun send(
        uuid: String,
        withdrawId: Long?,
        withdrawUuid: String?,
        lifecycleEventId: String?,
        walletTransactionRef: String?,
        businessReference: String?,
        currency: String,
        amount: BigDecimal,
        principalAmount: BigDecimal?,
        appliedCustomerFee: BigDecimal?,
        feeAsset: String?,
        transferCategory: String?,
        withdrawStatus: WithdrawStatus,
        createDate: LocalDateTime,
        lifecycleTransitionDate: LocalDateTime?,
        completionDate: LocalDateTime?,
        counterpartyReferenceId: String?
    ) {

    }
}