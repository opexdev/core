package co.nilin.opex.admin.app.data

data class EditCurrencyRequest(
    val symbol: String?,
    val precision: Double
){
    fun isValid(): Boolean {
        return !symbol.isNullOrEmpty() && precision > 0.0
    }
}