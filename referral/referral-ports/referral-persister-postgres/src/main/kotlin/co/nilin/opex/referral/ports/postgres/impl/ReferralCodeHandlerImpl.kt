package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.Referral
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import org.springframework.stereotype.Service

@Service
class ReferralCodeHandlerImpl : ReferralCodeHandler {
    override fun findReferralByUuid(uuid: String): Referral? {
        TODO("Not yet implemented")
    }
}