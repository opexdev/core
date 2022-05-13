package co.nilin.opex.utility.preferences

data class SystemWallet(
    var title: String = "",
    var level: String = "",
    var schedule: WalletSyncSchedule = WalletSyncSchedule()
)
