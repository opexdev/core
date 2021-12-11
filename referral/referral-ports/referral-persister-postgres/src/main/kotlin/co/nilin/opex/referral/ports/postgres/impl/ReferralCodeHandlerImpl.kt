package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.Referral
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ReferralCodeHandlerImpl : ReferralCodeHandler {
    override fun findAllReferrals(): List<Referral> {
        TODO("Not yet implemented")
    }

    override fun findReferralByUuid(uuid: String): Referral? {
        TODO("Not yet implemented")
    }

    override fun generateReferralCode(
        uuid: String,
        referrerCommission: BigDecimal,
        referentCommission: BigDecimal
    ): Referral {
        TODO("Not yet implemented")
    }

    override fun updateCommissions(
        code: String,
        referrerCommission: BigDecimal,
        referentCommission: BigDecimal
    ): Referral {
        TODO("Not yet implemented")
    }
}