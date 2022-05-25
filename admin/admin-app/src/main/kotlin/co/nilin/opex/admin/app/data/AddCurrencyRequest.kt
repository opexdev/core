package co.nilin.opex.admin.app.data

import java.math.BigDecimal

data class AddCurrencyRequest(
    val name: String?,
    val symbol: String?,
    val precision: BigDecimal
) {

    fun isValid(): Boolean {
        return !name.isNullOrEmpty() && !symbol.isNullOrEmpty() && precision > BigDecimal.ZERO && precision <= BigDecimal.ONE
    }

}