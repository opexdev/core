package co.nilin.opex.profile.app.dto

import co.nilin.opex.profile.core.data.profile.BankAccountStatus
import java.time.LocalDateTime

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
