package co.nilin.opex.wallet.core.model

data class VoucherGroup(
    var id: Long? = null,
    var issuer: String,
    var description: String? = null,
    var status: VoucherGroupStatus = VoucherGroupStatus.ACTIVE,
    var type: VoucherGroupType = VoucherGroupType.GIFT,
    var remainingUsage: Int? = null,
    var userLimit: Int? = null,
    var version: Long? = null
)
