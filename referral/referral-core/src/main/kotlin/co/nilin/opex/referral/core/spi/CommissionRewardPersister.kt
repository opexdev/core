package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.CommissionReward

interface CommissionRewardPersister {
    suspend fun save(commissionReward: CommissionReward)
}