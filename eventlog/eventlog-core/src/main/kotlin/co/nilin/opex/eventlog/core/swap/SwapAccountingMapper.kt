package co.nilin.opex.eventlog.core.swap

import co.nilin.opex.eventlog.core.accounting.*

/** Derive customer OUT/IN views without persisting duplicate operations. */
fun SwapRecord.toAccountingEvents(): List<AccountingTransactionEvent> {
    val swap = this
    val sourceTransactionId = swap.sourceTransactionId
    val destinationTransactionId = swap.destinationTransactionId
    val receiver = swap.receiverPartyId ?: swap.uuid
    val common = AccountingTransactionEvent(
        txId = sourceTransactionId,
        eventId = "swap-leg:${swap.txId}:OUT",
        userId = swap.uuid,
        type = AccountingTransactionType.SWAP,
        symbol = "${swap.sourceAsset}/${swap.destAsset}",
        asset = swap.sourceAsset,
        amount = swap.sourceAmount,
        fee = null,
        operationId = swap.txId,
        businessReference = swap.sourceReference,
        walletTransactionRef = swap.sourceTransactionId,
        relatedTransactionId = swap.destinationTransactionId,
        recordRole = "SWAP_LEG",
        senderPartyId = swap.uuid,
        receiverPartyId = swap.systemPartyId,
        perspectivePartyId = swap.uuid,
        senderWalletId = swap.sourceSenderWalletId,
        receiverWalletId = swap.sourceReceiverWalletId,
        senderWalletType = swap.sourceSenderWalletType,
        receiverWalletType = swap.sourceReceiverWalletType,
        sourceAsset = swap.sourceAsset,
        sourceAmount = swap.sourceAmount,
        destinationAsset = swap.destAsset,
        destinationAmount = swap.destAmount,
        source = "CORE_WALLET_KAFKA",
        referenceId = swap.sourceReference,
        side = "OUT",
        transferCategory = swap.transferCategory,
        operationCreatedAt = swap.quotedAt,
        lifecycleTransitionAt = swap.createDate,
        completedAt = swap.createDate,
        price = swap.quotedRate,
        quantity = null,
        occurredAt = swap.createDate
    )
    return listOf(common, common.copy(
            txId = destinationTransactionId,
            eventId = "swap-leg:${swap.txId}:IN",
            userId = receiver,
            asset = swap.destAsset,
            amount = swap.destAmount,
            businessReference = swap.destinationReference,
            walletTransactionRef = swap.destinationTransactionId,
            relatedTransactionId = swap.sourceTransactionId,
            senderPartyId = swap.systemPartyId,
            receiverPartyId = receiver,
            perspectivePartyId = receiver,
            senderWalletId = swap.destinationSenderWalletId,
            receiverWalletId = swap.destinationReceiverWalletId,
            senderWalletType = swap.destinationSenderWalletType,
            receiverWalletType = swap.destinationReceiverWalletType,
            referenceId = swap.destinationReference,
            side = "IN"
        ))
}
