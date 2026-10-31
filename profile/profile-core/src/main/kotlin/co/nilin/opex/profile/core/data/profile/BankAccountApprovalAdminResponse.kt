package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class BankAccountApprovalAdminResponse(
    var id: Long,
    var bankAccountId: Long,
    var userId: String,
    var status: BankAccountApprovalRequestStatus,
    var createDate: LocalDateTime,
    var updateDate: LocalDateTime? = null,
    var updater: String? = null,
    var description: String? = null,
    // The account under review, so the admin does not need a second call to see what to decide on
    var name: String? = null,
    var cardNumber: String? = null,
    var iban: String? = null,
    var accountNumber: String? = null,
    var bank: String? = null,
    var bankAccountStatus: BankAccountStatus? = null,
    var firstName: String? = null,
    var lastName: String? = null
)
