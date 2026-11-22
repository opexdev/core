package co.nilin.opex.eventlog.core.swap

import java.math.BigDecimal
import java.time.LocalDateTime

/** One complete OTC conversion. txId is the shared operation reference. */
data class SwapRecord(
    val uuid: String,
    val txId: String,
    val sourceAsset: String,
    val sourceAmount: BigDecimal,
    val destAsset: String,
    val destAmount: BigDecimal,
    val sourceTransactionId: String,
    val destinationTransactionId: String,
    val sourceReference: String? = null,
    val destinationReference: String? = null,
    val systemPartyId: String? = null,
    val receiverPartyId: String? = null,
    val sourceSenderWalletId: String? = null,
    val sourceReceiverWalletId: String? = null,
    val destinationSenderWalletId: String? = null,
    val destinationReceiverWalletId: String? = null,
    val sourceSenderWalletType: String? = null,
    val sourceReceiverWalletType: String? = null,
    val destinationSenderWalletType: String? = null,
    val destinationReceiverWalletType: String? = null,
    val reservationId: Long? = null,
    val reservationReference: String? = null,
    val quotedRate: BigDecimal? = null,
    val quotedAt: LocalDateTime? = null,
    val quoteExpiresAt: LocalDateTime? = null,
    val transferCategory: String? = null,
    val reservedBy: String? = null,
    val executedBy: String? = null,
    val description: String? = null,
    val status: String? = null,
    val createDate: LocalDateTime
)
