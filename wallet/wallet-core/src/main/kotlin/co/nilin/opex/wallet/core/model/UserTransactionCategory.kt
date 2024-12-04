package co.nilin.opex.wallet.core.model

enum class UserTransactionCategory {

    TRADE,
    DEPOSIT,
    DEPOSIT_TO, // for admin using DEPOSIT_MANUALLY
    WITHDRAW_FROM, // for admin using DEPOSIT_MANUALLY
    WITHDRAW,
    FEE,
    SWAP,
    SYSTEM
}