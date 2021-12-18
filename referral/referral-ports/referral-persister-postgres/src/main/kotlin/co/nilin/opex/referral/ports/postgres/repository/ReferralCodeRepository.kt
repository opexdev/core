package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.ports.postgres.dao.ReferralCode
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface ReferralCodeRepository : ReactiveCrudRepository<ReferralCode, Long> {
    fun findByCode(code: String): Mono<ReferralCode>
    fun findByUuid(uuid: String): Mono<ReferralCode>

    @Query("UPDATE referral_codes SET referrer_commission = COALESCE(:referrerCommission, referrer_commission), referent_commission = COALESCE(:referentCommission, referent_commission) WHERE code = :code")
    fun updateCommissions(code: String, referrerCommission: BigDecimal?, referentCommission: BigDecimal?): Mono<Void>

    fun deleteByUuid(uuid: String): Mono<Void>
    fun deleteByCode(code: String): Mono<Void>

    @Query("SELECT currval(pg_get_serial_sequence('referral_codes', 'id'))")
    fun findMaxId(): Mono<Long>
}