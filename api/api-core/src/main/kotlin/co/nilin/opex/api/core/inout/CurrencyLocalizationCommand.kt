package co.nilin.opex.api.core.inout

data class CurrencyLocalizationCommand(
    var id: Long? = null,
    var name: String? = null,
    var title: String? = null,
    var alias: String? = null,
    var description: String? = null,
    var shortDescription: String? = null,
    var language: String,
)
