package co.nilin.opex.eventlog.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("opex_swaps")
data class SwapModel(
    @Id val id: Long? = null,
    @Column("uuid") val uuid: String,
    @Column("tx_id") val txId: String,
    @Column("source_asset") val sourceAsset: String,
    @Column("source_amount") val sourceAmount: BigDecimal,
    @Column("dest_asset") val destAsset: String,
    @Column("dest_amount") val destAmount: BigDecimal,
    @Column("source_transaction_id") val sourceTransactionId: String,
    @Column("destination_transaction_id") val destinationTransactionId: String,
    @Column("source_reference") val sourceReference: String?,
    @Column("destination_reference") val destinationReference: String?,
    @Column("system_party_id") val systemPartyId: String?,
    @Column("receiver_party_id") val receiverPartyId: String?,
    @Column("source_sender_wallet_id") val sourceSenderWalletId: String?,
    @Column("source_receiver_wallet_id") val sourceReceiverWalletId: String?,
    @Column("destination_sender_wallet_id") val destinationSenderWalletId: String?,
    @Column("destination_receiver_wallet_id") val destinationReceiverWalletId: String?,
    @Column("source_sender_wallet_type") val sourceSenderWalletType: String?,
    @Column("source_receiver_wallet_type") val sourceReceiverWalletType: String?,
    @Column("destination_sender_wallet_type") val destinationSenderWalletType: String?,
    @Column("destination_receiver_wallet_type") val destinationReceiverWalletType: String?,
    @Column("reservation_id") val reservationId: Long?,
    @Column("reservation_reference") val reservationReference: String?,
    @Column("quoted_rate") val quotedRate: BigDecimal?,
    @Column("quoted_at") val quotedAt: LocalDateTime?,
    @Column("quote_expires_at") val quoteExpiresAt: LocalDateTime?,
    @Column("transfer_category") val transferCategory: String?,
    @Column("reserved_by") val reservedBy: String?,
    @Column("executed_by") val executedBy: String?,
    @Column("description") val description: String?,
    @Column("status") val status: String?,
    @Column("create_date") val createDate: LocalDateTime,
    @Column("source_topic") val sourceTopic: String,
    @Column("source_partition") val sourcePartition: Int,
    @Column("source_offset") val sourceOffset: Long,
    @Column("captured_at") val capturedAt: LocalDateTime
)
