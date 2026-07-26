package co.nilin.opex.common.service

import co.nilin.opex.common.data.UserLanguage.EN
import co.nilin.opex.utility.interceptors.spi.UserLanguageConfig
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class CustomUserLanguageConfig : UserLanguageConfig {
    override fun getDefaultLanguage(): String =
        runCatching {
            GlobalWebConfigCache.webConfig?.defaultLanguage?.name
        }.getOrNull() ?: EN.name
}