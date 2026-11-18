package co.nilin.opex.eventlog.app.controller

import co.nilin.opex.eventlog.core.accounting.AccountingEventFeedService
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionLogWriter
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionType
import co.nilin.opex.eventlog.app.service.AccountingTransactionKafkaPublisher
import co.nilin.opex.eventlog.ports.kafka.listener.inout.AccountingTransactionKafkaEvent
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDateTime

@RestController
@RequestMapping("/internal/eventlog/accounting")
class AccountingEventLogController(
    private val accountingEventFeedService: AccountingEventFeedService,
    private val accountingTransactionLogWriter: AccountingTransactionLogWriter,
    private val accountingTransactionKafkaPublisher: AccountingTransactionKafkaPublisher
) {

    @GetMapping("/transactions")
    suspend fun getTransactions(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        from: LocalDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        to: LocalDateTime?,
        @RequestParam(defaultValue = "2000")
        limit: Int
    ): AccountingEventLogResponse {
        val resolvedTo = to ?: LocalDateTime.now()
        val resolvedFrom = from ?: resolvedTo.minusDays(7)
        val data = accountingEventFeedService.findEvents(
            from = resolvedFrom,
            to = resolvedTo,
            limit = limit
        )
        return AccountingEventLogResponse(
            from = resolvedFrom,
            to = resolvedTo,
            items = data.map { it.toResponseItem() }
        )
    }

    @PostMapping("/transactions")
    suspend fun logTransaction(
        @RequestBody request: AccountingTransactionRequest
    ): AccountingEventLogItem {
        return accountingTransactionLogWriter.save(request.toCore()).toResponseItem()
    }

    @PostMapping("/transactions/batch")
    suspend fun logTransactions(
        @RequestBody request: AccountingTransactionBatchRequest
    ): AccountingTransactionBatchResponse {
        val saved = accountingTransactionLogWriter.saveAll(request.items.map { it.toCore() })
        return AccountingTransactionBatchResponse(
            total = saved.size,
            items = saved.map { it.toResponseItem() }
        )
    }

    @PostMapping("/transactions/publish")
    fun publishTransactionEvent(
        @RequestBody request: AccountingTransactionPublishRequest
    ): AccountingPublishResponse {
        accountingTransactionKafkaPublisher.publish(request.event.toKafkaEvent(), request.topic)
        return AccountingPublishResponse(published = 1)
    }

    @PostMapping("/transactions/publish/batch")
    fun publishTransactionEvents(
        @RequestBody request: AccountingTransactionPublishBatchRequest
    ): AccountingPublishResponse {
        request.items.forEach {
            accountingTransactionKafkaPublisher.publish(it.event.toKafkaEvent(), it.topic)
        }
        return AccountingPublishResponse(published = request.items.size)
    }

    private fun AccountingTransactionEvent.toResponseItem(): AccountingEventLogItem {
        return AccountingEventLogItem(
            txId = txId,
            eventId = eventId,
            userId = userId,
            type = type.name,
            symbol = symbol,
            asset = asset,
            amount = amount,
            fee = fee,
            feeAsset = feeAsset,
            principalAmount = principalAmount,
            grossAmount = grossAmount,
            withdrawStatus = withdrawStatus,
            transferCategory = transferCategory,
            operationId = operationId,
            businessReference = businessReference,
            walletTransactionRef = walletTransactionRef,
            recordRole = recordRole,
            senderPartyId = senderPartyId,
            receiverPartyId = receiverPartyId,
            senderWalletId = senderWalletId,
            receiverWalletId = receiverWalletId,
            senderWalletType = senderWalletType,
            receiverWalletType = receiverWalletType,
            perspectivePartyId = perspectivePartyId,
            counterpartyReferenceId = counterpartyReferenceId,
            relatedTransactionId = relatedTransactionId,
            sourceAsset = sourceAsset,
            sourceAmount = sourceAmount,
            destinationAsset = destinationAsset,
            destinationAmount = destinationAmount,
            originalTradeId = originalTradeId,
            baseAsset = baseAsset,
            quoteAsset = quoteAsset,
            quoteAmount = quoteAmount,
            tradeSide = tradeSide,
            participantRole = participantRole,
            orderReference = orderReference,
            counterpartyOrderReference = counterpartyOrderReference,
            financialActionId = financialActionId,
            relatedFinancialActionId = relatedFinancialActionId,
            feeActionId = feeActionId,
            expectedFee = expectedFee,
            expectedFeeAsset = expectedFeeAsset,
            source = source,
            referenceId = referenceId,
            side = side,
            price = price,
            quantity = quantity,
            operationCreatedAt = operationCreatedAt,
            lifecycleTransitionAt = lifecycleTransitionAt,
            completedAt = completedAt,
            occurredAt = occurredAt
        )
    }
}

data class AccountingEventLogResponse(
    val from: LocalDateTime,
    val to: LocalDateTime,
    val items: List<AccountingEventLogItem>
)

data class AccountingEventLogItem(
    val txId: String,
    val eventId: String? = null,
    val userId: String,
    val type: String,
    val symbol: String?,
    val asset: String,
    val amount: java.math.BigDecimal,
    val fee: java.math.BigDecimal?,
    val feeAsset: String?,
    val principalAmount: java.math.BigDecimal?,
    val grossAmount: java.math.BigDecimal?,
    val withdrawStatus: String?,
    val transferCategory: String?,
    val operationId: String?,
    val businessReference: String?,
    val walletTransactionRef: String?,
    val recordRole: String?,
    val senderPartyId: String? = null,
    val receiverPartyId: String? = null,
    val senderWalletId: String? = null,
    val receiverWalletId: String? = null,
    val senderWalletType: String? = null,
    val receiverWalletType: String? = null,
    val perspectivePartyId: String? = null,
    val counterpartyReferenceId: String? = null,
    val relatedTransactionId: String? = null,
    val sourceAsset: String? = null,
    val sourceAmount: BigDecimal? = null,
    val destinationAsset: String? = null,
    val destinationAmount: BigDecimal? = null,
    val originalTradeId: String? = null,
    val baseAsset: String? = null,
    val quoteAsset: String? = null,
    val quoteAmount: BigDecimal? = null,
    val tradeSide: String? = null,
    val participantRole: String? = null,
    val orderReference: String? = null,
    val counterpartyOrderReference: String? = null,
    val financialActionId: String? = null,
    val relatedFinancialActionId: String? = null,
    val feeActionId: String? = null,
    val expectedFee: BigDecimal? = null,
    val expectedFeeAsset: String? = null,
    val source: String,
    val referenceId: String?,
    val side: String?,
    val price: java.math.BigDecimal?,
    val quantity: java.math.BigDecimal?,
    val operationCreatedAt: LocalDateTime?,
    val lifecycleTransitionAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val occurredAt: LocalDateTime
)

data class AccountingTransactionRequest(
    val txId: String,
    val eventId: String? = null,
    val userId: String,
    val type: String,
    val symbol: String? = null,
    val asset: String,
    val amount: BigDecimal,
    val fee: BigDecimal? = null,
    val feeAsset: String? = null,
    val principalAmount: BigDecimal? = null,
    val grossAmount: BigDecimal? = null,
    val withdrawStatus: String? = null,
    val transferCategory: String? = null,
    val operationId: String? = null,
    val businessReference: String? = null,
    val walletTransactionRef: String? = null,
    val recordRole: String? = null,
    val senderPartyId: String? = null,
    val receiverPartyId: String? = null,
    val senderWalletId: String? = null,
    val receiverWalletId: String? = null,
    val senderWalletType: String? = null,
    val receiverWalletType: String? = null,
    val perspectivePartyId: String? = null,
    val counterpartyReferenceId: String? = null,
    val relatedTransactionId: String? = null,
    val sourceAsset: String? = null,
    val sourceAmount: BigDecimal? = null,
    val destinationAsset: String? = null,
    val destinationAmount: BigDecimal? = null,
    val originalTradeId: String? = null,
    val baseAsset: String? = null,
    val quoteAsset: String? = null,
    val quoteAmount: BigDecimal? = null,
    val tradeSide: String? = null,
    val participantRole: String? = null,
    val orderReference: String? = null,
    val counterpartyOrderReference: String? = null,
    val financialActionId: String? = null,
    val relatedFinancialActionId: String? = null,
    val feeActionId: String? = null,
    val expectedFee: BigDecimal? = null,
    val expectedFeeAsset: String? = null,
    val source: String,
    val referenceId: String? = null,
    val side: String? = null,
    val price: BigDecimal? = null,
    val quantity: BigDecimal? = null,
    val operationCreatedAt: LocalDateTime? = null,
    val lifecycleTransitionAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val occurredAt: LocalDateTime
) {
    fun toCore(): AccountingTransactionEvent {
        return AccountingTransactionEvent(
            txId = txId,
            eventId = eventId,
            userId = userId,
            type = AccountingTransactionType.valueOf(type.uppercase()),
            symbol = symbol,
            asset = asset,
            amount = amount,
            fee = fee,
            feeAsset = feeAsset,
            principalAmount = principalAmount,
            grossAmount = grossAmount,
            withdrawStatus = withdrawStatus,
            transferCategory = transferCategory,
            operationId = operationId,
            businessReference = businessReference,
            walletTransactionRef = walletTransactionRef,
            recordRole = recordRole,
            senderPartyId = senderPartyId,
            receiverPartyId = receiverPartyId,
            senderWalletId = senderWalletId,
            receiverWalletId = receiverWalletId,
            senderWalletType = senderWalletType,
            receiverWalletType = receiverWalletType,
            perspectivePartyId = perspectivePartyId,
            counterpartyReferenceId = counterpartyReferenceId,
            relatedTransactionId = relatedTransactionId,
            sourceAsset = sourceAsset,
            sourceAmount = sourceAmount,
            destinationAsset = destinationAsset,
            destinationAmount = destinationAmount,
            originalTradeId = originalTradeId,
            baseAsset = baseAsset,
            quoteAsset = quoteAsset,
            quoteAmount = quoteAmount,
            tradeSide = tradeSide,
            participantRole = participantRole,
            orderReference = orderReference,
            counterpartyOrderReference = counterpartyOrderReference,
            financialActionId = financialActionId,
            relatedFinancialActionId = relatedFinancialActionId,
            feeActionId = feeActionId,
            expectedFee = expectedFee,
            expectedFeeAsset = expectedFeeAsset,
            source = source,
            referenceId = referenceId,
            side = side,
            price = price,
            quantity = quantity,
            operationCreatedAt = operationCreatedAt,
            lifecycleTransitionAt = lifecycleTransitionAt,
            completedAt = completedAt,
            occurredAt = occurredAt
        )
    }
}

data class AccountingTransactionBatchRequest(
    val items: List<AccountingTransactionRequest>
)

data class AccountingTransactionBatchResponse(
    val total: Int,
    val items: List<AccountingEventLogItem>
)

data class AccountingTransactionPublishRequest(
    val topic: String? = null,
    val event: AccountingTransactionRequest
)

data class AccountingTransactionPublishBatchRequest(
    val items: List<AccountingTransactionPublishRequest>
)

data class AccountingPublishResponse(
    val published: Int
)

private fun AccountingTransactionRequest.toKafkaEvent(): AccountingTransactionKafkaEvent {
    return AccountingTransactionKafkaEvent(
        txId = txId,
        eventId = eventId,
        userId = userId,
        type = type.uppercase(),
        symbol = symbol,
        asset = asset,
        amount = amount,
        fee = fee,
        feeAsset = feeAsset,
        principalAmount = principalAmount,
        grossAmount = grossAmount,
        withdrawStatus = withdrawStatus,
        transferCategory = transferCategory,
        operationId = operationId,
        businessReference = businessReference,
        walletTransactionRef = walletTransactionRef,
        recordRole = recordRole,
        senderPartyId = senderPartyId,
        receiverPartyId = receiverPartyId,
        senderWalletId = senderWalletId,
        receiverWalletId = receiverWalletId,
        senderWalletType = senderWalletType,
        receiverWalletType = receiverWalletType,
        perspectivePartyId = perspectivePartyId,
        counterpartyReferenceId = counterpartyReferenceId,
        relatedTransactionId = relatedTransactionId,
        sourceAsset = sourceAsset,
        sourceAmount = sourceAmount,
        destinationAsset = destinationAsset,
        destinationAmount = destinationAmount,
        originalTradeId = originalTradeId,
        baseAsset = baseAsset,
        quoteAsset = quoteAsset,
        quoteAmount = quoteAmount,
        tradeSide = tradeSide,
        participantRole = participantRole,
        orderReference = orderReference,
        counterpartyOrderReference = counterpartyOrderReference,
        financialActionId = financialActionId,
        relatedFinancialActionId = relatedFinancialActionId,
        feeActionId = feeActionId,
        expectedFee = expectedFee,
        expectedFeeAsset = expectedFeeAsset,
        source = source,
        referenceId = referenceId,
        side = side,
        price = price,
        quantity = quantity,
        operationCreatedAt = operationCreatedAt,
        lifecycleTransitionAt = lifecycleTransitionAt,
        completedAt = completedAt,
        occurredAt = occurredAt
    )
}
