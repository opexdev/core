package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

enum class BankAccountApprovalRequestStatus {
    PENDING, APPROVED, REJECTED
}

data class BankAccountApprovalRequestFilter(
    val userId: String? = null,
    val status: BankAccountApprovalRequestStatus? = null,
    val createDateFrom: Long? = null,
    val createDateTo: Long? = null,
    val limit: Int = 10,
    val offset: Int = 0,
    val ascendingByTime: Boolean = false,
)

data class BankAccountApprovalAdminResponse(
    var id: Long,
    var bankAccountId: Long,
    var userId: String,
    var status: BankAccountApprovalRequestStatus,
    var createDate: LocalDateTime,
    var updateDate: LocalDateTime? = null,
    var updater: String? = null,
    var description: String? = null,
    var name: String? = null,
    var cardNumber: String? = null,
    var iban: String? = null,
    var accountNumber: String? = null,
    var bank: String? = null,
    var bankAccountStatus: BankAccountStatus? = null,
    var firstName: String? = null,
    var lastName: String? = null
)

data class BankAccountApprovalUserResponse(
    var bankAccountId: Long,
    var status: BankAccountApprovalRequestStatus,
    var createDate: LocalDateTime,
    var description: String? = null
)

data class UpdateBankAccountApprovalBody(
    val id: Long,
    val description: String? = null,
    val status: BankAccountApprovalRequestStatus
)
