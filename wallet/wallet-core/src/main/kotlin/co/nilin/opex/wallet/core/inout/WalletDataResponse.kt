package co.nilin.opex.wallet.core.inout

data class WalletDataResponse(
    val uuid: String,
    val title: String,
    val wallets: List<WalletCurrencyData>
)

data class WalletCurrencyData(
    val currency: String = "",
    val free: Double = 0.0,
    val locked: Double = 0.0,
    val pendingWithdraw: Double = 0.0
)

data class RawWalletDataResponse(
    val uuid: String,
    val title: String,
    val wallets: String
)