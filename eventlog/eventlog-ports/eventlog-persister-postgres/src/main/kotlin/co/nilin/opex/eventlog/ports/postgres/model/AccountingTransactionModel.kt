package co.nilin.opex.eventlog.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("opex_accounting_transactions")
data class AccountingTransactionModel(
    @Id val id: Long?,
    @Column("tx_id")
    val txId: String,
    @Column("event_id")
    val eventId: String?,
    @Column("user_id")
    val userId: String,
    val type: String,
    val symbol: String?,
    val asset: String,
    val amount: BigDecimal,
    val fee: BigDecimal?,
    @Column("fee_asset")
    val feeAsset: String?,
    @Column("principal_amount")
    val principalAmount: BigDecimal?,
    @Column("gross_amount")
    val grossAmount: BigDecimal?,
    @Column("withdraw_status")
    val withdrawStatus: String?,
    @Column("transfer_category")
    val transferCategory: String?,
    @Column("operation_id")
    val operationId: String?,
    @Column("business_ref")
    val businessReference: String?,
    @Column("wallet_tx_ref")
    val walletTransactionRef: String?,
    @Column("record_role")
    val recordRole: String?,
    @Column("sender_party_id")
    val senderPartyId: String? = null,
    @Column("receiver_party_id")
    val receiverPartyId: String? = null,
    @Column("sender_wallet_id")
    val senderWalletId: String? = null,
    @Column("receiver_wallet_id")
    val receiverWalletId: String? = null,
    @Column("sender_wallet_type")
    val senderWalletType: String? = null,
    @Column("receiver_wallet_type")
    val receiverWalletType: String? = null,
    @Column("perspective_party_id")
    val perspectivePartyId: String? = null,
    @Column("counterparty_reference_id")
    val counterpartyReferenceId: String? = null,
    @Column("related_transaction_id")
    val relatedTransactionId: String? = null,
    @Column("source_asset")
    val sourceAsset: String? = null,
    @Column("source_amount")
    val sourceAmount: BigDecimal? = null,
    @Column("destination_asset")
    val destinationAsset: String? = null,
    @Column("destination_amount")
    val destinationAmount: BigDecimal? = null,
    @Column("original_trade_id")
    val originalTradeId: String? = null,
    @Column("base_asset")
    val baseAsset: String? = null,
    @Column("quote_asset")
    val quoteAsset: String? = null,
    @Column("quote_amount")
    val quoteAmount: BigDecimal? = null,
    @Column("trade_side")
    val tradeSide: String? = null,
    @Column("participant_role")
    val participantRole: String? = null,
    @Column("order_reference")
    val orderReference: String? = null,
    @Column("counterparty_order_reference")
    val counterpartyOrderReference: String? = null,
    @Column("financial_action_id")
    val financialActionId: String? = null,
    @Column("related_financial_action_id")
    val relatedFinancialActionId: String? = null,
    @Column("fee_action_id")
    val feeActionId: String? = null,
    @Column("expected_fee")
    val expectedFee: BigDecimal? = null,
    @Column("expected_fee_asset")
    val expectedFeeAsset: String? = null,
    val source: String,
    @Column("reference_id")
    val referenceId: String?,
    val side: String?,
    val price: BigDecimal?,
    val quantity: BigDecimal?,
    @Column("operation_created_at")
    val operationCreatedAt: LocalDateTime?,
    @Column("lifecycle_transition_at")
    val lifecycleTransitionAt: LocalDateTime?,
    @Column("completed_at")
    val completedAt: LocalDateTime?,
    @Column("occurred_at")
    val occurredAt: LocalDateTime,
    @Column("create_date")
    val createDate: LocalDateTime,
    val transferMethod: String? = null,
    val applicator: String? = null,
    val depositStatus: String? = null,
    val operationType: String? = null
)
