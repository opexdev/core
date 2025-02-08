package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("voucher_group")
data class VoucherGroupModel(
    @Id val id: Long? = null,
    val issuer: String,
    var description: String? = null
)