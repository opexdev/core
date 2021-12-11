package co.nilin.opex.referral.ports.postgres.impl

import co.nilin.opex.referral.core.model.Config
import co.nilin.opex.referral.core.spi.ConfigHandler
import org.springframework.stereotype.Service

@Service
class ConfigHandlerImpl : ConfigHandler {
    override fun findConfig(name: String): Config? {
        TODO("Not yet implemented")
    }
}