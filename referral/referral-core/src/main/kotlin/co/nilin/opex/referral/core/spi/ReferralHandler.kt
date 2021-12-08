package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Referral

interface ReferralHandler {
    fun findReferralByUuid(uuid: String): Referral
}
