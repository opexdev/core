package co.nilin.opex.wallet.ports.kafka.listener.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class SwapEvent(
    val uuid: String,
    val txId: String,
    val sourceAsset: String,
    val sourceAmount: BigDecimal,
    val destAsset: String,
    val destAmount: BigDecimal,
    val sourceTransactionId: String? = null,
    val destinationTransactionId: String? = null,
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
    val createDate: LocalDateTime = LocalDateTime.now()
)
