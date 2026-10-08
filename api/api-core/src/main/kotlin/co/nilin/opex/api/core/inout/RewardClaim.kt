package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

enum class CheckoutStatus {
    PENDING, SUCCESS, FAILED
}

enum class RewardType {
    KYC_LEVEL_UPDATED, TRADE, USER_CREATED, REFERRAL_INVITE
}

data class RewardClaimDto(
    val id: Long?,
    val rewardConfig: String,
    val rewardedUuid: String,
    val rewardType: RewardType,
    val transferRef: String,
    val checkoutStatus: CheckoutStatus?,
    val createDate: LocalDateTime?,
    val updateDate: LocalDateTime?
)

data class RewardClaimCheckoutErrorDto(
    val id: Long?,
    val rewardClaimsId: Long,
    val errorMessage: String,
    val createDate: LocalDateTime?
)

data class UpdateRewardClaimStatusRequest(val status: CheckoutStatus)
