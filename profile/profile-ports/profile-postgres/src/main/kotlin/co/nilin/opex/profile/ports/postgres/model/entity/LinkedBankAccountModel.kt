package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.Limitation
import co.nilin.opex.profile.ports.postgres.model.base.LinkedBankAccount
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("linked_bank_account")
data class LinkedBankAccountModel(
        @Id var id: Long) : LinkedBankAccount()
