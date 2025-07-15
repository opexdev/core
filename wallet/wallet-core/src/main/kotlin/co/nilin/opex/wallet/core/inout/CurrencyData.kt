package co.nilin.opex.wallet.core.inout


import java.math.BigDecimal
import java.util.*

data class CurrencyData(
    var symbol: String,
    var uuid: String? = UUID.randomUUID().toString(),
    var name: String,
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
    var order: Int? = null,
    var maxOrder: BigDecimal? = null,

    )
