package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class BankAccountApprovalAdminResponse(
    var id: Long,
    var bankAccountId: Long,
    var userId: String,
    var status: BankAccountStatus? = null,
    var createDate: LocalDateTime,
    var updateDate: LocalDateTime? = null,
    var updater: String? = null,
    var description: String? = null,
    var name: String? = null,
    var cardNumber: String? = null,
    var iban: String? = null,
    var accountNumber: String? = null,
    var bank: String? = null,
    var firstName: String? = null,
    var lastName: String? = null
)
