package co.nilin.opex.wallet.core.model

interface Wallet {
    fun id(): Long?
    fun owner(): WalletOwner
    fun balance(): Amount
    fun currency(): Currency
    fun type(): String
}