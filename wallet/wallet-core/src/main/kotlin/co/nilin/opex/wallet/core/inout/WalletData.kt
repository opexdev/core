package co.nilin.opex.wallet.core.inout

data class WalletData(
    val uuid: String,
    val title: String,
    val walletType: String,
    val currency: String,
    val balance: Double
)
