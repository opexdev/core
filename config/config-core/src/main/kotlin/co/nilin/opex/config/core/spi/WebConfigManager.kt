package co.nilin.opex.config.core.spi

import co.nilin.opex.config.core.inout.WebConfig

interface WebConfigManager {

    fun getConfig(): WebConfig

    fun updateConfig(config: WebConfig): WebConfig

}