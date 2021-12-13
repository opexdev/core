package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.ports.postgres.dao.Referent
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface ReferralRepository : ReactiveCrudRepository<Referent, Long> {
    fun findByCode(code: String): Flux<Referent>
    fun findByUuid(uuid: String): Flux<Referent>
    fun deleteByUuid(uuid: String)
    fun deleteByCode(code: String)
}