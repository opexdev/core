package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import co.nilin.opex.referral.ports.postgres.repository.CommissionRewardRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class CommissionRewardHandlerImpl(
    private val commissionRewardRepository: CommissionRewardRepository
) : CommissionRewardHandler {
    override suspend fun findCommissions(
        referralCode: String?,
        rewardedUuid: String?,
        referentUuid: String?
    ): List<CommissionReward> {
        return commissionRewardRepository.findByReferralCodeAndRewardedUuidAndReferentUuid(
            referralCode,
            rewardedUuid,
            referentUuid
        ).map {
            CommissionReward(
                it.id!!,
                it.rewardedUuid,
                it.referentUuid,
                it.referralCode,
                Pair(it.richTradeId, null),
                it.referentOrderDirection,
                it.share,
                it.paymentCurrency,
                it.createDate!!
            )
        }.collectList().awaitSingleOrNull() ?: emptyList()
    }

    override suspend fun deleteCommissions(referralCode: String?, rewardedUuid: String?, referentUuid: String?) {
        commissionRewardRepository.deleteByReferralCodeAndRewardedUuidAndReferentUuid(
            referralCode,
            rewardedUuid,
            referentUuid
        ).awaitSingleOrNull()
    }

    override suspend fun deleteCommissionById(id: Long) {
        commissionRewardRepository.deleteById(id).awaitSingleOrNull()
    }
}
