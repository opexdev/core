package co.nilin.opex.profile.ports.postgres.model.base

import java.time.LocalDateTime

open class BankAccountApprovalRequest {
    var bankAccountId: Long = 0
    lateinit var userId: String
    var createDate: LocalDateTime? = null
    var updateDate: LocalDateTime? = null
    var updater: String? = null
    var description: String? = null
}
