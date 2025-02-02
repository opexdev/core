package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.VoucherStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("voucher")
data class VoucherModel(
    @Id val id: Long? = null,
    @Column("private_code") val privateCode: String,
    @Column("public_code") val publicCode: String,
    val amount: BigDecimal,
    val currency: String,
    var status: VoucherStatus,
    @Column("expire_date") val expireDate: LocalDateTime,
    @Column("create_date") val createDate: LocalDateTime = LocalDateTime.now(),
    @Column("use_date") var useDate: LocalDateTime? = null,
    @Column("user_id") var userId: String? = null,
    val description: String? = null
)