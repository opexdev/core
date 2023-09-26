package co.nilin.opex.config.ports.redis.utils

import co.nilin.opex.config.core.inout.SystemConfig
import co.nilin.opex.config.core.inout.UserWebConfig
import co.nilin.opex.config.ports.redis.document.SystemConfigDocument
import co.nilin.opex.config.ports.redis.document.UserWebConfigDocument

fun UserWebConfigDocument.asDTO() = UserWebConfig(theme, language, favoritePairs)

fun SystemConfigDocument.asDTO() = SystemConfig(
    logoUrl,
    title,
    description,
    defaultLanguage,
    supportedLanguages,
    defaultTheme,
    supportEmail,
    baseCurrency,
    dateType
)