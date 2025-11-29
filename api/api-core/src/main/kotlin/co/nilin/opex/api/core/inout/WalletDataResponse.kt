package co.nilin.opex.api.core.inout

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