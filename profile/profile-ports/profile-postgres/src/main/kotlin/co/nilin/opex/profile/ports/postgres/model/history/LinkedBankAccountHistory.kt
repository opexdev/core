package co.nilin.opex.profile.ports.postgres.model.history

import co.nilin.opex.profile.ports.postgres.model.base.Limitation
import co.nilin.opex.profile.ports.postgres.model.base.LinkedBankAccount
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("linked_bank_account_history")
data class LinkedBankAccountHistory(
        @Id
        var id: Long,
        var issuer: String?,
        var changeRequestDate: LocalDateTime?,
        var changeRequestType: String?
) : LinkedBankAccount()