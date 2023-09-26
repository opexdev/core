package co.nilin.opex.config.app.dto

data class UpdateSystemConfigRequest(
    val logoUrl: String?,
    val title: String?,
    val description: String?,
    val defaultLanguage: String?,
    val supportedLanguages: List<String>?,
    val defaultTheme: String?,
    val supportEmail: String?,
    val baseCurrency: String?,
    val dateType: String?,
)