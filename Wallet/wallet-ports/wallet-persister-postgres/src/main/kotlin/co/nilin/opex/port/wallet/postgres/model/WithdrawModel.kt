package co.nilin.opex.port.wallet.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("withdraws")
class WithdrawModel(@Id var id: Long?,
                    @Column("transaction_id") val transactionId: String,
                    @Column("wallet") val wallet: Long,
                    @Column("amount") val amount: BigDecimal,
                    @Column("fee") val fee: BigDecimal,
                    @Column("net_amount") val netAmount: BigDecimal,
                    @Column("dest_currency") val destCurrency: String?,
                    @Column("dest_address") val destAddress: String?,
                    @Column("dest_notes") var destNote: String?,
                    @Column("dest_transaction_ref") var destTransactionRef: String?,
                    @Column("description") val description: String?,
                    @Column("status_reason") var statusReason: String?,
                    @Column("status") var status: String
)