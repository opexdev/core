package co.nilin.opex.wallet.core.model

enum class TransferCategory {

    NONE,
    DEPOSIT,
    DEPOSIT_MANUALLY,
    WITHDRAW_REQUEST,
    WITHDRAW_ACCEPT,
    WITHDRAW_REJECT,
    PURCHASE_FINALIZED,

    ORDER_CREATE,
    ORDER_CANCEL,
    ORDER_FINALIZED,
    TRADE,
    FEE,

    NORMAL //TODO TEST?
}