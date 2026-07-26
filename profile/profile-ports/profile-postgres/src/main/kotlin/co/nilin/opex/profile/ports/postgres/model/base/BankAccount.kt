package co.nilin.opex.profile.ports.postgres.model.base

import co.nilin.opex.profile.core.data.profile.BankAccountStatus
import java.time.LocalDateTime

open class BankAccount {
    lateinit var uuid: String
    var name: String? = null
    var cardNumber: String? = null
    var iban: String? = null
    var accountNumber: String? = null
    var bank: String? = null
    lateinit var status: BankAccountStatus
    lateinit var createDate: LocalDateTime
    var updateDate: LocalDateTime? = null
    var creator: String? = null

}