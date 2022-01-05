package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.CommissionRewardPersister
import co.nilin.opex.referral.ports.postgres.repository.CommissionRewardRepository
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class CommissionRewardPersisterImpl(private val commissionRewardRepository: CommissionRewardRepository) :
    CommissionRewardPersister {
    override suspend fun save(commissionReward: CommissionReward) {
        commissionRewardRepository.save(
            co.nilin.opex.referral.ports.postgres.dao.CommissionReward(
                if (commissionReward.id == 0L) null else commissionReward.id,
                commissionReward.rewardedUuid,
                commissionReward.referentUuid,
                commissionReward.referralCode,
                commissionReward.richTrade.first,
                commissionReward.referentOrderDirection,
                commissionReward.share
            )
        ).awaitSingleOrNull()
    }
}
