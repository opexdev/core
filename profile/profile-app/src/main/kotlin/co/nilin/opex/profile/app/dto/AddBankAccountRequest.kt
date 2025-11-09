package co.nilin.opex.profile.app.dto

data class AddBankAccountRequest(
    val name: String? = null,
    val cardNumber: String? = null,
    val iban: String? = null,
)
