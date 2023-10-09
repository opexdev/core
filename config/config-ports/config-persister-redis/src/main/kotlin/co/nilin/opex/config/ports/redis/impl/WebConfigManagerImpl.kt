package co.nilin.opex.config.ports.redis.impl

import co.nilin.opex.config.core.inout.WebConfig
import co.nilin.opex.config.core.spi.WebConfigManager
import co.nilin.opex.config.ports.redis.dao.WebConfigRepository
import co.nilin.opex.config.ports.redis.document.WebConfigDocument
import co.nilin.opex.config.ports.redis.utils.asDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WebConfigManagerImpl(private val webConfigRepository: WebConfigRepository) : WebConfigManager {

    private val logger = LoggerFactory.getLogger(WebConfigManagerImpl::class.java)

    override fun getConfig(): WebConfig {
        return getConfigOrCreate().asDTO()
    }

    override fun updateConfig(config: WebConfig): WebConfig {
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
        return webConfigRepository.save(savedConfig).asDTO()
    }

    private fun getConfigOrCreate(): WebConfigDocument {
        return webConfigRepository.findById(WebConfigDocument.ID)
            .orElseGet {
                logger.info("Unable to find config for system. Creating new config object.")
                webConfigRepository.save(WebConfigDocument.default())
            }
    }
}