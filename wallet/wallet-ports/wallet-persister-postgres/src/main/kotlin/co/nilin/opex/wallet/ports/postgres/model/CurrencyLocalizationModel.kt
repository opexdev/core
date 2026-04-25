package co.nilin.opex.wallet.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("currency_localization")
data class CurrencyLocalizationModel(
    @Id
    var id: Long? = null,
    var currency: String,
    var name: String? = null,
    var title: String? = null,
    var alias: String? = null,
    var description: String? = null,
    var shortDescription: String? = null,
    var language: String,

    )