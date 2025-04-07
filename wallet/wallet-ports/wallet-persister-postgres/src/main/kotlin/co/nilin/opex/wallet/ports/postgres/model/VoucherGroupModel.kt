package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.VoucherGroupStatus
import co.nilin.opex.wallet.core.model.VoucherGroupType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table

@Table("voucher_group")
data class VoucherGroupModel(
    @Id val id: Long? = null,
    val issuer: String,
    var description: String? = null,
    var status: VoucherGroupStatus = VoucherGroupStatus.ACTIVE,
    val type: VoucherGroupType = VoucherGroupType.GIFT,
    var remainingUsage: Int? = null,
    var userLimit: Int? = null,
    @Version
    var version: Long? = null
)