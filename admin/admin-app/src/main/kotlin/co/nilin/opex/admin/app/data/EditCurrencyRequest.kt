package co.nilin.opex.admin.app.data

import java.math.BigDecimal

data class EditCurrencyRequest(
        val symbol: String?,
        val precision: BigDecimal
) {
    fun isValid(): Boolean {
        return !symbol.isNullOrEmpty() && precision > BigDecimal.ZERO
    }
}