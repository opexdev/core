package co.nilin.opex.admin.app.data

data class AddCurrencyRequest(
    val name: String?,
    val symbol: String?,
    val precision: Double
) {

    fun isValid(): Boolean {
        return !name.isNullOrEmpty() && !symbol.isNullOrEmpty() && precision > 0.0
    }

}