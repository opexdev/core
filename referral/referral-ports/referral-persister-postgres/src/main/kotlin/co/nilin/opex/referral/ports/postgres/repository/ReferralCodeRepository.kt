package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.ports.postgres.dao.ReferralCode
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface ReferralCodeRepository : ReactiveCrudRepository<ReferralCode, Long> {

    @Query("SELECT MAX(id) FROM referral_codes")
    fun findMaxId(): Mono<Long>
    fun findByCode(code: String): Mono<ReferralCode>
    fun findByUuid(uuid: String): Mono<ReferralCode>

    @Query("UPDATE referral_codes SET referent_commission = COALESCE(:referentCommission, referent_commission) WHERE code = :code")
    fun updateCommissions(code: String, referentCommission: BigDecimal?): Mono<Void>

    fun deleteByUuid(uuid: String): Mono<Void>
    fun deleteByCode(code: String): Mono<Void>
}
