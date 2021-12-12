package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Referral
import java.math.BigDecimal

interface ReferralCodeHandler {
    suspend fun generateReferralCode(uuid: String, referrerCommission: BigDecimal, referentCommission: BigDecimal)
    suspend fun findAllReferrals(): List<Referral>
    suspend fun findReferralByUuid(uuid: String): Referral?
    suspend fun findReferralByCode(code: String): Referral?
    suspend fun assign(code: String, referentUuid: String): Referral
    suspend fun updateCommissions(code: String, referrerCommission: BigDecimal?, referentCommission: BigDecimal?)
    suspend fun deleteReferralCode(code: String)
    suspend fun deleteReferralCodeByUuid(uuid: String)
}
