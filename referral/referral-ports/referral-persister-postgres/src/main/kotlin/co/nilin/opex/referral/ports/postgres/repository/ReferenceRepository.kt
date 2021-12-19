package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.ports.postgres.dao.Reference
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ReferenceRepository : ReactiveCrudRepository<Reference, Long> {
    @Query("SELECT * FROM referral_code_references LEFT JOIN referral_codes ON referral_code_id = referral_codes.id")
    fun findByCode(code: String): Flux<Reference>
    fun findByUuid(uuid: String): Flux<Reference>
    fun deleteByUuid(uuid: String)
    fun deleteByReferralCodeId(id: Long): Flux<Reference>
}
