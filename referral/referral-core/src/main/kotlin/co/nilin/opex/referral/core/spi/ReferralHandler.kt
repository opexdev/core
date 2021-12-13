package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Referral

interface ReferralHandler {
    suspend fun findAllReferrals(): List<Referral>
    suspend fun findReferralByUuid(uuid: String): Referral?
    suspend fun findReferralCodeByCode(code: String): Referral?
}
