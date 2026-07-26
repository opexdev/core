package co.nilin.opex.api.core.inout

data class AddBankAccountRequest(
    val name: String? = null,
    val cardNumber: String? = null,
    val iban: String? = null,
)
