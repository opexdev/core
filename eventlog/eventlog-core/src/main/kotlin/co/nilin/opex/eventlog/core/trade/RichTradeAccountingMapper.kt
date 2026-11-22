package co.nilin.opex.eventlog.core.trade

import co.nilin.opex.eventlog.core.accounting.*

/** Read-time participant views; the complete RichTrade is the only stored execution source. */
fun RichTradeRecord.toAccountingEvents(): List<AccountingTransactionEvent> {
    val trade = this
    val execution = executionReference
    val base = baseAsset
    val quote = quoteAsset
    fun side(direction: String) = when (direction.uppercase()) {
        "ASK" -> "SELL"
        "BID" -> "BUY"
        else -> error("Unsupported trade direction: $direction")
    }
    val takerSide = side(takerDirection)
    val makerSide = side(makerDirection)
    val quoteAmount = trade.matchedPrice.multiply(trade.matchedQuantity)
    val seller = if (takerSide == "SELL") trade.takerUuid else trade.makerUuid
    val buyer = if (takerSide == "BUY") trade.takerUuid else trade.makerUuid
    fun participant(taker: Boolean): AccountingTransactionEvent {
        val role = if (taker) "TAKER" else "MAKER"
        val action = if (taker) trade.takerSettlementActionId else trade.makerSettlementActionId
        val otherAction = if (taker) trade.makerSettlementActionId else trade.takerSettlementActionId
        val direction = if (taker) takerSide else makerSide
        val user = if (taker) trade.takerUuid else trade.makerUuid
        return AccountingTransactionEvent(
            txId = "$execution:$role", eventId = "$execution:$role", operationId = execution,
            originalTradeId = trade.originalTradeId.toString(), userId = user,
            type = AccountingTransactionType.TRADE, symbol = trade.pair,
            asset = base, amount = trade.matchedQuantity,
            baseAsset = base, quoteAsset = quote, quoteAmount = quoteAmount,
            price = trade.matchedPrice, quantity = trade.matchedQuantity,
            tradeSide = direction, participantRole = role,
            orderReference = if (taker) trade.takerOuid else trade.makerOuid,
            counterpartyOrderReference = if (taker) trade.makerOuid else trade.takerOuid,
            financialActionId = action, relatedFinancialActionId = otherAction,
            feeActionId = if (taker) trade.takerFeeActionId else trade.makerFeeActionId,
            businessReference = action?.let { "accountant:fiActions:$it" },
            // Assessed commission is not evidence that the fee transfer completed.
            fee = null,
            expectedFee = if (taker) trade.takerCommision else trade.makerCommision,
            expectedFeeAsset = if (taker) trade.takerCommisionAsset else trade.makerCommisionAsset,
            recordRole = "TRADE_EXECUTION", senderPartyId = seller, receiverPartyId = buyer,
            perspectivePartyId = user,
            // IN/OUT is relative to this participant and the row's base asset.
            side = if (direction == "SELL") "OUT" else "IN",
            sourceAsset = if (direction == "SELL") base else quote,
            sourceAmount = if (direction == "SELL") trade.matchedQuantity else quoteAmount,
            destinationAsset = if (direction == "SELL") quote else base,
            destinationAmount = if (direction == "SELL") quoteAmount else trade.matchedQuantity,
            source = "CORE_ACCOUNTANT_TRADE", referenceId = execution,
            occurredAt = trade.tradeDateTime
        )
    }

    return listOf(participant(true), participant(false))
}
