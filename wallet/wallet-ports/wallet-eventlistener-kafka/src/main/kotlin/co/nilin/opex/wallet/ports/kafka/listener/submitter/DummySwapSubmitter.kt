package co.nilin.opex.wallet.ports.kafka.listener.submitter

import co.nilin.opex.wallet.core.spi.SwapEventSubmitter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(name = ["app.swap.event.enabled"], havingValue = "false")
class DummySwapSubmitter : SwapEventSubmitter {
    override suspend fun send(
        uuid: String,
        txId: String,
        sourceAsset: String,
        sourceAmount: BigDecimal,
        destAsset: String,
        destAmount: BigDecimal,
        createDate: LocalDateTime,
        sourceTransactionId: String?,
        destinationTransactionId: String?,
        sourceReference: String?,
        destinationReference: String?,
        systemPartyId: String?,
        receiverPartyId: String?,
        sourceSenderWalletId: String?,
        sourceReceiverWalletId: String?,
        destinationSenderWalletId: String?,
        destinationReceiverWalletId: String?,
        sourceSenderWalletType: String?,
        sourceReceiverWalletType: String?,
        destinationSenderWalletType: String?,
        destinationReceiverWalletType: String?
    ) {
    }
}
