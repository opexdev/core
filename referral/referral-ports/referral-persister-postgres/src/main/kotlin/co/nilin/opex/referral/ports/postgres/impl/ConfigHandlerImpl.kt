package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.Config
import co.nilin.opex.referral.core.spi.ConfigHandler
import co.nilin.opex.referral.ports.postgres.repository.ConfigRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class ConfigHandlerImpl(private val configRepository: ConfigRepository) : ConfigHandler {
    override suspend fun findConfig(name: String): Config? {
        return configRepository.findById(name)
            .map {
                Config(
                    it.name,
                    it.referralCommissionReward,
                    it.paymentCurrency,
                    it.minPaymentAmount,
                    it.paymentWindowSeconds,
                    it.maxReferralCodePerUser
                )
            }
            .awaitSingleOrNull()
    }
}
