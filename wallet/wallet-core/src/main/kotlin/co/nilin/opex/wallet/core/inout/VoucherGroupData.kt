package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.VoucherGroupStatus
import co.nilin.opex.wallet.core.model.VoucherGroupType

data class VoucherGroupData(
    var issuer: String,
    var description: String? = null,
    var status: VoucherGroupStatus,
    var type: VoucherGroupType,
    var remainingUsage: Int? = null,
    var userLimit: Int? = null,
)
