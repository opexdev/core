package co.nilin.opex.api.core.inout

data class AppConfig(
    val apiMethods: List<ApiMethod>,
    val accounts: List<AccountType>
) {

    enum class ApiMethod {
        NATIVE, PODIUM, GOOGLE
    }

    enum class AccountType {
        BANK_ACCOUNT, ADDRESS_BOOK, DESTINATION_ID, BANK_CONTRACT
    }
}
