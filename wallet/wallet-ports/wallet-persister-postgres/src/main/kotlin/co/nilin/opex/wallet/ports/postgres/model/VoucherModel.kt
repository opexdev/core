package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("voucher")
data class VoucherModel(
    @Id val id: Long? = null,
    val privateCode: String,
    val publicCode: String,
    val amount: BigDecimal,
    val currency: String,
    val expireDate: LocalDateTime,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val voucherGroup: Long
)