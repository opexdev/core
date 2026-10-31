package co.nilin.opex.profile.ports.postgres.model.base

import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequestStatus
import java.time.LocalDateTime

open class BankAccountApprovalRequest {
    var bankAccountId: Long = 0
    lateinit var userId: String
    var status: BankAccountApprovalRequestStatus? = BankAccountApprovalRequestStatus.PENDING
    var createDate: LocalDateTime? = null
    var updateDate: LocalDateTime? = null
    var updater: String? = null
    var description: String? = null
}
