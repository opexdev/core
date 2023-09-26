package co.nilin.opex.config.ports.redis.impl

import co.nilin.opex.config.core.inout.SystemConfig
import co.nilin.opex.config.core.spi.SystemConfigManager
import co.nilin.opex.config.ports.redis.dao.SystemConfigRepository
import co.nilin.opex.config.ports.redis.document.SystemConfigDocument
import co.nilin.opex.config.ports.redis.utils.asDTO
import org.springframework.stereotype.Component

@Component
class SystemConfigManagerImpl(private val systemConfigRepository: SystemConfigRepository) : SystemConfigManager {

    override fun getConfig(): SystemConfig {
        return getConfigOrCreate().asDTO()
    }

    override fun updateConfig(config: SystemConfig): SystemConfig {
        val savedConfig = getConfigOrCreate()
        config.apply {
            logoUrl?.let { savedConfig.logoUrl = it }
            title?.let { savedConfig.title = it }
            description?.let { savedConfig.description = it }
            defaultLanguage?.let { savedConfig.defaultLanguage = it }
            supportedLanguages?.let { savedConfig.supportedLanguages = it }
            defaultTheme?.let { savedConfig.defaultTheme = it }
            supportEmail?.let { savedConfig.supportEmail = it }
            baseCurrency?.let { savedConfig.baseCurrency = it }
            dateType?.let { savedConfig.dateType = it }
        }
        return systemConfigRepository.save(savedConfig).asDTO()
    }

    private fun getConfigOrCreate(): SystemConfigDocument {
        return systemConfigRepository.findById(SystemConfigDocument.ID)
            .orElseGet { systemConfigRepository.save(SystemConfigDocument.default()) }
    }
}