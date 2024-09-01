package co.nilin.opex.wallet.core.model

enum class TransferCategory {

    NO_CATEGORY,
    DEPOSIT,
    DEPOSIT_MANUALLY,
    WITHDRAW_REQUEST,
    WITHDRAW_ACCEPT,
    WITHDRAW_REJECT,
    WITHDRAW_CANCEL,
    PURCHASE_FINALIZED,

    ORDER_CREATE,
    ORDER_CANCEL,
    ORDER_FINALIZED,
    TRADE,
    FEE,

    NORMAL //TODO TEST?
}