package co.nilin.opex.port.wallet.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("withdraws")
class WithdrawModel(@Id var id: Long?,
                    @Column("uuid") val ownerUuid: String,
                    @Column("wallet") val wallet: Long,
                    @Column("amount") val amount: BigDecimal,
                    @Column("req_transaction_id") val requestTransaction: String,
                    @Column("final_transaction_id") val finalizedTransaction: String?,
                    @Column("accepted_fee") val acceptedFee: BigDecimal,
                    @Column("applied_fee") val appliedFee: BigDecimal?,
                    @Column("net_amount") val netAmount: BigDecimal?,
                    @Column("dest_currency") val destCurrency: String?,
                    @Column("dest_network") val destNetwork: String?,
                    @Column("dest_address") val destAddress: String?,
                    @Column("dest_notes") var destNote: String?,
                    @Column("dest_transaction_ref") var destTransactionRef: String?,
                    @Column("status_reason") var statusReason: String?,
                    @Column("status") var status: String
)