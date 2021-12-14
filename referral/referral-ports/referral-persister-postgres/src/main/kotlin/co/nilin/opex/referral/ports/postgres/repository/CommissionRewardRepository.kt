package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.ports.postgres.dao.CommissionReward
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CommissionRewardRepository : ReactiveCrudRepository<CommissionReward, Long> {
    fun findByReferrer(referrerUuid: String): Flux<CommissionReward>
    fun findByReferent(referentUuid: String): Flux<CommissionReward>
    fun findByReferralCode(referralCode: String): Flux<CommissionReward>
    fun deleteByReferrerUuid(referrerUuid: String): Mono<Void>
    fun deleteByReferentUuid(referentUuid: String): Mono<Void>
    fun deleteByReferralCode(referentCode: String): Mono<Void>
}