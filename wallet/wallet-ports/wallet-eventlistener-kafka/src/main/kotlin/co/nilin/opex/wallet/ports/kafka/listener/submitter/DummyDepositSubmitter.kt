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
        depositUuid: String?,
        lifecycleEventId: String?,
        walletTransactionRef: String?,
        businessReference: String?,
        currency: String,
        amount: BigDecimal,
        principalAmount: BigDecimal?,
        appliedCustomerFee: BigDecimal?,
        feeAsset: String?,
        transferCategory: String?,
        status: String?,
        createDate: LocalDateTime?,
        lifecycleTransitionDate: LocalDateTime?,
        completionDate: LocalDateTime?,
        depositId: Long?,
        transferMethod: String?,
        operationType: String?,
        applicator: String?,
        gatewayId: String?,
        network: String?,
        note: String?,
        attachment: String?,
        senderPartyId: String?,
        receiverPartyId: String?,
        senderWalletId: String?,
        receiverWalletId: String?,
        senderWalletType: String?,
        receiverWalletType: String?,
        acceptedFee: BigDecimal?,
        sourceSymbol: String?,
        sourceAddress: String?
    ) {

    }
}