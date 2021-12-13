package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.ports.postgres.dao.ReferralCode
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.math.BigDecimal

interface ReferralCodeRepository : ReactiveCrudRepository<ReferralCode, Long> {
    fun findByCode(code: String): Mono<ReferralCode>
    fun findByUuid(uuid: String): Mono<ReferralCode>

    @Query("UPDATE referral_codes SET referrer_commission = COALESCE(:referrerCommission, referrer_commission), referent_commission = COALESCE(:referentCommission, referent_commission) WHERE code = :code")
    fun updateCommissions(code: String, referrerCommission: BigDecimal?, referentCommission: BigDecimal?)

    fun deleteByUuid(uuid: String)
    fun deleteByCode(code: String)

    @Query("SELECT currval(pg_get_serial_sequence('referral_codes', 'id'))")
    fun findMaxId(): Mono<Long>
}