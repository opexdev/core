package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("voucher_usage")
data class VoucherUsageModel(
    @Id val id: Long? = null,
    val voucher: Long,
    var useDate: LocalDateTime,
    var uuid: String,
)