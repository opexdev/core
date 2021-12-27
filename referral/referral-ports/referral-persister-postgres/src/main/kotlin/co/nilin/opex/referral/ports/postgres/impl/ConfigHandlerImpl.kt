package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.Config
import co.nilin.opex.referral.core.spi.ConfigHandler
import co.nilin.opex.referral.ports.postgres.repository.ConfigRepository
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class ConfigHandlerImpl(private val configRepository: ConfigRepository) : ConfigHandler {
    override suspend fun findConfig(name: String): Config? {
        return configRepository.findById(name)
            .map { Config(it.name, it.referralCommissionReward, it.paymentAssetSymbol) }.awaitSingleOrNull()
    }
}