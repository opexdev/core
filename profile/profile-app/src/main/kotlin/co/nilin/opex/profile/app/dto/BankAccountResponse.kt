package co.nilin.opex.profile.app.dto

import co.nilin.opex.profile.core.data.profile.BankAccountStatus

data class BankAccountResponse(
    var id: Long? = null,
    var name: String? = null,
    var cardNumber: String? = null,
    var iban: String? = null,
    var accountNumber: String? = null,
    var bank: String? = null,
    var status: BankAccountStatus,
)
