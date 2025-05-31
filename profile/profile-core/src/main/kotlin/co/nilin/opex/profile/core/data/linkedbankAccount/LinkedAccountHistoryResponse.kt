package co.nilin.opex.profile.core.data.linkedbankAccount

import java.time.LocalDateTime

data class LinkedAccountHistoryResponse(
    var userId: String?,
    var bankAccountType: BankAccountType,
    var registerDate: LocalDateTime? = null,
    var verifiedDate: LocalDateTime? = null,
    var number: String,
    var accountId: String? = null,
    var enabled: Boolean?,
    var verified: Boolean?,
    var description: String?,
    var changeRequestDate: LocalDateTime?,
    var changeRequestType: String?
)
