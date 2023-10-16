package co.nilin.opex.config.ports.redis.utils

import co.nilin.opex.config.core.inout.WebConfig
import co.nilin.opex.config.core.inout.UserWebConfig
import co.nilin.opex.config.ports.redis.document.WebConfigDocument
import co.nilin.opex.config.ports.redis.document.UserWebConfigDocument

fun UserWebConfigDocument.asDTO() = UserWebConfig(theme, language, favoritePairs)

fun WebConfigDocument.asDTO() = WebConfig(
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