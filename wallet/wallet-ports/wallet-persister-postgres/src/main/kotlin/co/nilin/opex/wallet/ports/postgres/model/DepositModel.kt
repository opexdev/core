package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("deposits")
data class DepositModel(
    @Id var id: Long?,
    @Column("uuid")
    val ownerUuid: String,
    @Column("duid")
    val depositUuid: String,
    val currency: String,
    val amount: BigDecimal,
    val acceptedFee: BigDecimal?,
    val appliedFee: BigDecimal?,
    val sourceSymbol: String?,
    val network: String?,
    val sourceAddress: String?,
    var note: String?,
    var transactionRef: String?,
    var status: String?,
    var depositType: String?,
    val createDate: LocalDateTime? = LocalDateTime.now()
)
