package co.nilin.opex.config.core.spi

import co.nilin.opex.config.core.inout.SystemConfig

interface SystemConfigManager {

    fun getConfig(): SystemConfig

    fun updateConfig(config: SystemConfig): SystemConfig

}