package co.nilin.opex.api.core.inout

enum class UserTransactionCategory {

    TRADE,
    DEPOSIT,
    DEPOSIT_TO, // for admin using DEPOSIT_MANUALLY
    WITHDRAW_FROM, // for admin using DEPOSIT_MANUALLY
    WITHDRAW,
    FEE,
    SWAP,
    REFERRAL_COMMISSION,
    REFERRAL_KYC_REWARD,
    REFERENT_COMMISSION,
    KYC_ACCEPTED_REWARD,
    SYSTEM
}