package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Reference

interface ReferralHandler {
    suspend fun findAllReferrals(): List<Reference>
    suspend fun findReferralByUuid(uuid: String): Reference?
    suspend fun findReferralCodeByCode(code: String): Reference?
}
