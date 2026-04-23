package co.nilin.opex.wallet.ports.postgres.dto


import java.math.BigDecimal

data class CurrencyView(
    var symbol: String,
    var uuid: String,
    var name: String? = null,
    var precision: BigDecimal,
    var title: String? = null,
    var alias: String? = null,
    var icon: String? = null,
    var isTransitive: Boolean? = false,
    var isActive: Boolean? = true,
    var sign: String? = null,
    var description: String? = null,
    var shortDescription: String? = null,
    var externalUrl: String? = null,
    var displayOrder: Int? = null,
    var maxOrder: BigDecimal? = null,
)