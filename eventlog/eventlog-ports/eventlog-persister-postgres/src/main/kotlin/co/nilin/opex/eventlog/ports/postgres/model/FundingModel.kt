package co.nilin.opex.eventlog.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("opex_funding_operations")
data class FundingModel(
    @Id val id: Long? = null,
    @Column("event_id") val eventId: String,
    @Column("operation_id") val operationId: String,
    @Column("type") val type: String,
    @Column("user_id") val userId: String,
    @Column("operation_database_id") val operationDatabaseId: Long?,
    @Column("currency") val currency: String,
    @Column("amount") val amount: BigDecimal,
    @Column("principal_amount") val principalAmount: BigDecimal?,
    @Column("applied_customer_fee") val appliedCustomerFee: BigDecimal?,
    @Column("fee_asset") val feeAsset: String?,
    @Column("accepted_fee") val acceptedFee: BigDecimal?,
    @Column("status") val status: String,
    @Column("transfer_category") val transferCategory: String?,
    @Column("transfer_method") val transferMethod: String?,
    @Column("operation_type") val operationType: String?,
    @Column("applicator") val applicator: String?,
    @Column("gateway_id") val gatewayId: String?,
    @Column("network") val network: String?,
    @Column("business_reference") val businessReference: String?,
    @Column("counterparty_reference_id") val counterpartyReferenceId: String?,
    @Column("wallet_transaction_ref") val walletTransactionRef: String?,
    @Column("request_transaction_id") val requestTransactionId: String?,
    @Column("finalized_transaction_id") val finalizedTransactionId: String?,
    @Column("operation_wallet_id") val operationWalletId: String?,
    @Column("sender_party_id") val senderPartyId: String?,
    @Column("receiver_party_id") val receiverPartyId: String?,
    @Column("sender_wallet_id") val senderWalletId: String?,
    @Column("receiver_wallet_id") val receiverWalletId: String?,
    @Column("sender_wallet_type") val senderWalletType: String?,
    @Column("receiver_wallet_type") val receiverWalletType: String?,
    @Column("source_symbol") val sourceSymbol: String?,
    @Column("source_address") val sourceAddress: String?,
    @Column("destination_address") val destinationAddress: String?,
    @Column("destination_asset") val destinationAsset: String?,
    @Column("destination_amount") val destinationAmount: BigDecimal?,
    @Column("note") val note: String?,
    @Column("status_reason") val statusReason: String?,
    @Column("attachment") val attachment: String?,
    @Column("otp_required") val otpRequired: Int?,
    @Column("operation_created_at") val operationCreatedAt: LocalDateTime?,
    @Column("lifecycle_transition_at") val lifecycleTransitionAt: LocalDateTime?,
    @Column("completed_at") val completedAt: LocalDateTime?,
    @Column("occurred_at") val occurredAt: LocalDateTime,
    @Column("source_topic") val sourceTopic: String,
    @Column("source_partition") val sourcePartition: Int,
    @Column("source_offset") val sourceOffset: Long,
    @Column("captured_at") val capturedAt: LocalDateTime
)
