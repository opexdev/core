package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Referral
import java.math.BigDecimal

interface ReferralCodeHandler {
    fun findAllReferrals(): List<Referral>
    fun findReferralByUuid(uuid: String): Referral?
    fun generateReferralCode(uuid: String, referrerCommission: BigDecimal, referentCommission: BigDecimal): Referral
    fun updateCommissions(code: String, referrerCommission: BigDecimal, referentCommission: BigDecimal): Referral
}
