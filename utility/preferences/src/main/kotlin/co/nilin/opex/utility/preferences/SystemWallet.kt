package co.nilin.opex.utility.preferences

data class SystemWallet(
    var title: String = "system",
    var level: String = "basic",
    var schedule: WalletSyncSchedule = WalletSyncSchedule()
)
