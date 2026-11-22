package co.nilin.opex.eventlog.core.funding

import co.nilin.opex.eventlog.core.accounting.*

fun FundingRecord.toAccountingEvent(): AccountingTransactionEvent {
    val deposit = type == "DEPOSIT"
    return AccountingTransactionEvent(
        txId = eventId, eventId = eventId, userId = userId,
        type = AccountingTransactionType.valueOf(type), symbol = null, asset = currency, amount = amount,
        fee = appliedCustomerFee, feeAsset = feeAsset, principalAmount = principalAmount, grossAmount = amount,
        depositStatus = if (deposit) status else null, withdrawStatus = if (deposit) null else status,
        transferCategory = transferCategory, transferMethod = transferMethod, operationType = operationType,
        applicator = applicator, operationId = operationId, businessReference = businessReference,
        walletTransactionRef = walletTransactionRef,
        relatedTransactionId = if (status == "DONE") requestTransactionId else finalizedTransactionId,
        recordRole = if (deposit) "DEPOSIT_LIFECYCLE" else "WITHDRAW_LIFECYCLE",
        senderPartyId = senderPartyId ?: if (deposit) null else userId,
        receiverPartyId = receiverPartyId ?: if (deposit) userId else null,
        senderWalletId = senderWalletId, receiverWalletId = receiverWalletId,
        senderWalletType = senderWalletType, receiverWalletType = receiverWalletType,
        perspectivePartyId = userId, counterpartyReferenceId = counterpartyReferenceId,
        sourceAsset = if (deposit) sourceSymbol else currency,
        sourceAmount = if (deposit) null else principalAmount,
        destinationAsset = if (deposit) currency else destinationAsset,
        destinationAmount = if (deposit) amount else destinationAmount,
        source = if (deposit) "CORE_WALLET_DEPOSIT" else "CORE_WALLET_WITHDRAW",
        referenceId = if (deposit) counterpartyReferenceId else operationId,
        side = if (status == "DONE") { if (deposit) "IN" else "OUT" } else null,
        price = null, quantity = null, operationCreatedAt = operationCreatedAt,
        lifecycleTransitionAt = lifecycleTransitionAt, completedAt = completedAt, occurredAt = occurredAt
    )
}
