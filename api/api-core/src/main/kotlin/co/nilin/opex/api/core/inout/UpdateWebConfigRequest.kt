package co.nilin.opex.api.core.inout

import co.nilin.opex.common.data.CalenderType
import co.nilin.opex.common.data.LanguageOption
import co.nilin.opex.common.data.Theme
import co.nilin.opex.common.data.UserLanguage

data class UpdateWebConfigRequest(
    val logoUrl: String?,
    val title: String?,
    val description: String?,
    var defaultLanguage: UserLanguage?,
    var supportedLanguages: Map<UserLanguage, LanguageOption>?,
    var supportedCalenders: List<CalenderType>?,
    val defaultTheme: Theme?,
    val supportedThemes: List<Theme>?,
    val supportEmail: String?,
)