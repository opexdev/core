package co.nilin.opex.config.core.model

data class GeneralSystemConfig(
    val logoUrl: String,
    val title: String,
    val description: String,
    val defaultLanguage: String,
    val supportedLanguages: List<String>,
    val defaultTheme: String,
    val supportEmail: String,
    val baseCurrency: String,
    val dateType: String
)