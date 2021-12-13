package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Referral
import java.math.BigDecimal

interface ReferralCodeHandler {
    suspend fun generateReferralCode(
        uuid: String,
        referrerCommission: BigDecimal,
        referentCommission: BigDecimal
    ): String

    suspend fun findAllReferralCodes(): List<Referral>
    suspend fun findReferralByUuid(uuid: String): Referral?
    suspend fun findReferralCodeByCode(code: String): Referral?
    suspend fun assign(code: String, referentUuid: String): Referral
    suspend fun updateCommissions(code: String, referrerCommission: BigDecimal, referentCommission: BigDecimal)
    suspend fun deleteReferralCodeByCode(code: String)
    suspend fun deleteReferralCodeByUuid(uuid: String)
}
