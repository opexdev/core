package co.nilin.opex.profile.ports.postgres.model.base

import co.nilin.opex.profile.core.data.linkedbankAccount.BankAccountType
import java.time.LocalDateTime

open class LinkedBankAccount {
    lateinit var userId: String
    var bankAccountType: BankAccountType? = null
    var registerDate: LocalDateTime? = null
    var verifiedDate: LocalDateTime? = null
    var enabled: Boolean? = false
    var verified: Boolean? = false
    var verifier: String? = null
    var number: String? = null
    var accountId: String? = null
    var description: String? = null
}