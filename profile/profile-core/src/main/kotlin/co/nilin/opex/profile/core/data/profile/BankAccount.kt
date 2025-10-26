package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class BankAccount(
    var id: Long? = null,
    var uuid: String,
    var name: String? = null,
    var cardNumber: String? = null,
    var iban: String? = null,
    var accountNumber: String? = null,
    var bank: String? = null,
    var status: BankAccountStatus,
    var createDate: LocalDateTime,
    var updateDate: LocalDateTime? = null,
    var creator: String? = null,
)