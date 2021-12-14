package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.CommissionReward

interface CommissionRewardHandler {
    suspend fun findCommissions(
        referralCode: String? = null,
        referrerUuid: String? = null,
        referentUuid: String? = null
    ): List<CommissionReward>

    suspend fun deleteCommissions(
        referralCode: String? = null,
        referrerUuid: String? = null,
        referentUuid: String? = null
    )

    suspend fun deleteCommissionById(id: Long)
}
