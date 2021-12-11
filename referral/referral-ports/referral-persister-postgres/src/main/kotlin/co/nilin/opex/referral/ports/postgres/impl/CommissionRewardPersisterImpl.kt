package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.CommissionRewardPersister

class CommissionRewardPersisterImpl : CommissionRewardPersister {
    override suspend fun save(commissionReward: CommissionReward) {
        TODO("Not yet implemented")
    }
}