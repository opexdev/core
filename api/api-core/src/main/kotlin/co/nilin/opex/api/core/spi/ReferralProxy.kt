package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.CheckoutStatus
import co.nilin.opex.api.core.inout.ReferralCodeDto
import co.nilin.opex.api.core.inout.RewardClaimCheckoutErrorDto
import co.nilin.opex.api.core.inout.RewardClaimDto

interface ReferralProxy {

    // User
    suspend fun getCommissionShareSteps(): List<Int>
    suspend fun generateReferralCode(token: String, commissionShare: Int): String
    suspend fun getReferralCode(code: String): ReferralCodeDto
    suspend fun getReferralCodes(token: String): List<ReferralCodeDto>

    // Admin
    suspend fun getRewardClaims(
        token: String,
        status: CheckoutStatus?,
        uuid: String?,
        offset: Int,
        limit: Int
    ): List<RewardClaimDto>

    suspend fun getRewardClaimErrors(token: String, id: Long): List<RewardClaimCheckoutErrorDto>
    suspend fun retryRewardClaim(token: String, id: Long): RewardClaimDto
    suspend fun updateRewardClaimStatus(token: String, id: Long, status: CheckoutStatus): RewardClaimDto
}
