package co.nilin.opex.api.core.inout

data class CreateReferralCodeRequest(val commissionShare: Int)

data class ReferralCodeDto(
    val uuid: String,
    val code: String,
    val referrerShare: Int,
    val referentShare: Int,
    val referentsCount: Int
)
