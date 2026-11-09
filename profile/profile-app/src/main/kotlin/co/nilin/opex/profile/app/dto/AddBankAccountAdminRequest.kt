package co.nilin.opex.profile.app.dto

data class AddBankAccountAdminRequest(
    val name: String? = null,
    val cardNumber: String? = null,
    val iban: String? = null,
    val accountNumber: String? = null,
    val bank: String? = null,
)
