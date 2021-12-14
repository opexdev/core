package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import co.nilin.opex.referral.ports.postgres.repository.CommissionRewardRepository
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service

@Service
class CommissionRewardHandlerImpl(
    private val commissionRewardRepository: CommissionRewardRepository
) : CommissionRewardHandler {
    override suspend fun findAllCommissions(): List<CommissionReward> {
        return commissionRewardRepository.findAll().map {
            CommissionReward(
                it.referrerUuid,
                it.referentUuid,
                it.referralCode,
                null,
                it.referrerShare,
                it.referentShare
            )
        }.collectList().awaitSingle()
    }

    override suspend fun findCommissionsByReferrer(uuid: String, code: String?): List<CommissionReward> {
        return commissionRewardRepository.findByReferrer(uuid).filter { code == null || it.referralCode == code }.map {
            CommissionReward(
                it.referrerUuid,
                it.referentUuid,
                it.referralCode,
                null,
                it.referrerShare,
                it.referentShare
            )
        }.collectList().awaitSingle()
    }

    override suspend fun findCommissionsByReferent(uuid: String): List<CommissionReward> {
        return commissionRewardRepository.findByReferent(uuid).map {
            CommissionReward(
                it.referrerUuid,
                it.referentUuid,
                it.referralCode,
                null,
                it.referrerShare,
                it.referentShare
            )
        }.collectList().awaitSingle()
    }

    override suspend fun deleteCommissionsByReferrer(uuid: String) {
        commissionRewardRepository.deleteByReferrerUuid(uuid)
    }

    override suspend fun deleteCommissionsByReferent(uuid: String) {
        commissionRewardRepository.deleteByReferentUuid(uuid)
    }

    override suspend fun deleteCommissionsByCode(code: String) {
        commissionRewardRepository.deleteByReferralCode(code)
    }

    override suspend fun deleteAllCommissions() {
        commissionRewardRepository.deleteAll()
    }

    override suspend fun deleteCommissionById(id: Long) {
        commissionRewardRepository.deleteById(id)
    }
}
