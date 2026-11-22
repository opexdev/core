package co.nilin.opex.wallet.core.spi

import java.math.BigDecimal
import java.time.LocalDateTime

interface SwapEventSubmitter {
    suspend fun send(
        uuid: String,
        txId: String,
        sourceAsset: String,
        sourceAmount: BigDecimal,
        destAsset: String,
        destAmount: BigDecimal,
        createDate: LocalDateTime = LocalDateTime.now(),
        sourceTransactionId: String? = null,
        destinationTransactionId: String? = null,
        sourceReference: String? = null,
        destinationReference: String? = null,
        systemPartyId: String? = null,
        receiverPartyId: String? = null,
        sourceSenderWalletId: String? = null,
        sourceReceiverWalletId: String? = null,
        destinationSenderWalletId: String? = null,
        destinationReceiverWalletId: String? = null,
        sourceSenderWalletType: String? = null,
        sourceReceiverWalletType: String? = null,
        destinationSenderWalletType: String? = null,
        destinationReceiverWalletType: String? = null,
        reservationId: Long? = null,
        reservationReference: String? = null,
        quotedRate: BigDecimal? = null,
        quotedAt: LocalDateTime? = null,
        quoteExpiresAt: LocalDateTime? = null,
        transferCategory: String? = null,
        reservedBy: String? = null,
        executedBy: String? = null,
        description: String? = null,
        status: String? = null
    )
}
