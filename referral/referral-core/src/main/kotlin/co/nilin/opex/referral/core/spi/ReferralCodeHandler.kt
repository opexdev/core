package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Referral
import java.math.BigDecimal

interface ReferralCodeHandler {
    fun findAllReferrals(): List<Referral>
    fun findReferralByUuid(uuid: String): Referral?
    fun findReferralByCode(code: String): Referral?
    fun generateReferralCode(uuid: String, referrerCommission: BigDecimal, referentCommission: BigDecimal): Referral
    fun assign(code: String, referentUuid: String): Referral
    fun updateCommissions(code: String, referrerCommission: BigDecimal?, referentCommission: BigDecimal?): Referral
    fun deleteReferralCode(code: String)
    fun deleteReferralCodeByUuid(uuid: String)
}
