package co.nilin.opex.wallet.core.inout

data class WalletDataResponse(
    val uuid: String,
    val title: String,
    val wallets: List<WalletCurrencyData>
)

data class WalletCurrencyData(
    val currency: String,
    val free: Double,
    val locked: Double,
    val pendingWithdraw: Double
)
