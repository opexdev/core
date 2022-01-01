package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.ports.postgres.dao.CommissionReward
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CommissionRewardRepository : ReactiveCrudRepository<CommissionReward, Long> {
    @Query("SELECT * FROM commission_rewards WHERE (:code is null OR referral_code = :code) AND (:rewardedUuid is null OR rewarded_uuid = :rewardedUuid) AND (:referentUuid is null OR referent_uuid = :referentUuid)")
    fun findByReferralCodeAndRewardedUuidAndReferentUuid(
        code: String?,
        rewardedUuid: String?,
        referentUuid: String?
    ): Flux<CommissionReward>

    @Query("DELETE FROM commission_rewards WHERE (:code is null OR referral_code = :code) AND (:rewardedUuid is null OR rewarded_uuid = :rewardedUuid) AND (:referentUuid is null OR referent_uuid = :referentUuid)")
    fun deleteByReferralCodeAndRewardedUuidAndReferentUuid(
        code: String?,
        rewardedUuid: String?,
        referentUuid: String?
    ): Mono<Void>
}