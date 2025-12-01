package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.BankAccount
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("bank_account")
data class BankAccountModel(
    @Id var id: Long
) : BankAccount()
