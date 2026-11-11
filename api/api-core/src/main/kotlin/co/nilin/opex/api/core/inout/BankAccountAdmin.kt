package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

data class AddBankAccountAdminRequest(
    val name: String? = null,
    val cardNumber: String? = null,
    val iban: String? = null,
    val accountNumber: String? = null,
    val bank: String? = null,
)

data class BankAccountAdminResponse(
    val id: Long,
    val uuid: String,
    val name: String?,
    val cardNumber: String?,
    val iban: String?,
    val accountNumber: String?,
    val bank: String?,
    val status: BankAccountStatus,
    val createDate: LocalDateTime?,
    val updateDate: LocalDateTime?,
    val creator: String?
)
