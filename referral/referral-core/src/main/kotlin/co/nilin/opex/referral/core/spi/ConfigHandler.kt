package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.Config

interface ConfigHandler {
    suspend fun findConfig(name: String): Config?
}
