package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import co.nilin.opex.referral.ports.postgres.repository.CommissionRewardRepository
import kotlinx.coroutines.reactive.awaitSingleOrDefault
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class CommissionRewardHandlerImpl(
    private val commissionRewardRepository: CommissionRewardRepository
) : CommissionRewardHandler {
    override suspend fun findCommissions(
        referralCode: String?,
        referrerUuid: String?,
        referentUuid: String?
    ): List<CommissionReward> {
        return commissionRewardRepository.findByReferralCodeAndReferrerUuidAndReferentUuid(
            referralCode,
            referrerUuid,
            referentUuid
        ).map {
            CommissionReward(
                it.referrerUuid,
                it.referentUuid,
                it.referralCode,
                Pair(it.richTradeId, null),
                it.referentOrderDirection,
                it.referrerShare,
                it.referentShare,
                it.paymentAssetSymbol
            )
        }.collectList().awaitSingleOrDefault(emptyList())
    }

    override suspend fun deleteCommissions(referralCode: String?, referrerUuid: String?, referentUuid: String?) {
        commissionRewardRepository.deleteByReferralCodeAndReferrerUuidAndReferentUuid(
            referralCode,
            referrerUuid,
            referentUuid
        ).awaitSingleOrNull()
    }

    override suspend fun deleteCommissionById(id: Long) {
        commissionRewardRepository.deleteById(id).awaitSingleOrNull()
    }
}
