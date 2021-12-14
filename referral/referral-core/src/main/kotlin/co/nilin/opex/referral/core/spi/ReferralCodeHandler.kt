package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.ReferralCode
import java.math.BigDecimal

interface ReferralCodeHandler {
    suspend fun generateReferralCode(
        uuid: String,
        referrerCommission: BigDecimal,
        referentCommission: BigDecimal
    ): String

    suspend fun findAllReferralCodes(): List<ReferralCode>
    suspend fun findReferralCodeByReferentUuid(uuid: String): ReferralCode?
    suspend fun findReferralCodeByCode(code: String): ReferralCode?
    suspend fun assign(code: String, referentUuid: String)
    suspend fun updateCommissions(code: String, referrerCommission: BigDecimal, referentCommission: BigDecimal)
    suspend fun deleteReferralCodeByCode(code: String)
    suspend fun deleteReferralCodesByReferrerUuid(uuid: String)
}
