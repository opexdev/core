package co.nilin.opex.common.data

data class WebConfig(
    val logoUrl: String?,
    val title: String?,
    val description: String?,
    var defaultLanguage: UserLanguage?,
    var supportedLanguages: Map<UserLanguage, LanguageOption>?,
    var supportedCalenders: List<CalenderType>?,
    val defaultTheme: Theme?,
    var supportedThemes: List<Theme>?,
    val supportEmail: String?,
)