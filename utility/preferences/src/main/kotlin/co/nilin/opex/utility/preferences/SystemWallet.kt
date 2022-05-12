package co.nilin.opex.utility.preferences

import java.util.*

data class SystemWallet(
    var uuid: String = UUID.randomUUID().toString(),
    var title: String = "",
    var level: String = "",
    var schedule: WalletSyncSchedule = WalletSyncSchedule()
)
