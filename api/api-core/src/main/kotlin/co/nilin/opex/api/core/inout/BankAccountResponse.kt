package co.nilin.opex.api.core.inout


data class BankAccountResponse(
    var id: Long? = null,
    var name: String? = null,
    var cardNumber: String? = null,
    var iban: String? = null,
    var accountNumber: String? = null,
    var bank: String? = null,
    var status: BankAccountStatus,
)

enum class BankAccountStatus {
    WAITING, VERIFIED, REJECTED
}