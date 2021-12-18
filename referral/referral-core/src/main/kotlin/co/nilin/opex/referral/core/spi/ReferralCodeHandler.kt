package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.ReferralCode
import java.math.BigDecimal

interface ReferralCodeHandler {
    suspend fun generateReferralCode(
        uuid: String,
        referrerCommission: BigDecimal,
        referentCommission: BigDecimal
    ): String

    suspend fun findAll(): List<ReferralCode>
    suspend fun findByReferentUuid(uuid: String): ReferralCode?
    suspend fun findByCode(code: String): ReferralCode?
    suspend fun assign(code: String, referentUuid: String)
    suspend fun updateCommissions(code: String, referrerCommission: BigDecimal, referentCommission: BigDecimal)
    suspend fun deleteByCode(code: String)
    suspend fun deleteByReferrerUuid(uuid: String)
}
