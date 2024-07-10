package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("deposits")
data class DepositModel(@Id var id: Long?,
                        @Column("uuid") val ownerUuid: String,
                        @Column("duid") val depositUuid: String,
                        @Column("currency") val currency: String,
                        @Column("amount") val amount: BigDecimal,
                        @Column("accepted_fee") val acceptedFee: BigDecimal?,
                        @Column("applied_fee") val appliedFee: BigDecimal?,
                        @Column("source_symbol") val sourceSymbol: String?,
                        @Column("network") val network: String?,
                        @Column("source_address") val sourceAddress: String?,
                        @Column("note") var note: String?,
                        @Column("transaction_ref") var transactionRef:String?,
                        @Column("status") var status: String?,
                        @Column("deposit_type") var depositType: String?,
                        @Column("create_date") val createDate: LocalDateTime? = LocalDateTime.now()
)
