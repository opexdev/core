package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.ports.postgres.dao.Referral
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface ReferralRepository : ReactiveCrudRepository<Referral, Long> {
    fun findByCode(code: String): Flux<Referral>
    fun findByUuid(uuid: String): Flux<Referral>
    fun deleteByUuid(uuid: String)
    fun deleteByCode(code: String)
}