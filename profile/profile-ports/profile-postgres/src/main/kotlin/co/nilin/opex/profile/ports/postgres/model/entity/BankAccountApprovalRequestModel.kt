package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.BankAccountApprovalRequest
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("bank_account_approval_request")
data class BankAccountApprovalRequestModel(
    @Id var id: Long
) : BankAccountApprovalRequest()
