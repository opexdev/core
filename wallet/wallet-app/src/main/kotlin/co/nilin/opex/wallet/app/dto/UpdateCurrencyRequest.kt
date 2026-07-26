package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class UpdateCurrencyRequest(
    var symbol: String? = null,
    var precision: BigDecimal,
    var icon: String? = null,
    var isTransitive: Boolean? = false,
    var isActive: Boolean? = true,
    var sign: String? = null,
    var externalUrl: String? = null,
    var displayOrder: Int? = null,
    var maxOrder: BigDecimal? = null,

    ) {
    fun toCurrencyDto(): CurrencyDto {
       return CurrencyDto(
            symbol = symbol,
            precision = precision,
            icon = icon,
            isTransitive = isTransitive,
            isActive = isActive,
            sign = sign,
            externalUrl = externalUrl,
            displayOrder = displayOrder,
            maxOrder = maxOrder,
        )
    }
}
