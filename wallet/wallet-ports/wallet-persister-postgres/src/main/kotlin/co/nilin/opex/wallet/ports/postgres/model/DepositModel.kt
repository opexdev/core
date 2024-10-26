package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.DepositStatus
import co.nilin.opex.wallet.core.model.DepositType
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
    var status: DepositStatus,
    val attachment:String?,
    var depositType: DepositType,
    val createDate: LocalDateTime = LocalDateTime.now(),
)
